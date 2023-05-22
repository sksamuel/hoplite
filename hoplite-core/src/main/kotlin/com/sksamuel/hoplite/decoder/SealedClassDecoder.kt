package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.plus
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.valueOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class SealedClassDecoder : NullHandlingDecoder<Any> {

  override fun supports(type: KType): Boolean {
    return when (type.classifier is KClass<*>) {
      true -> {
        val kclass = type.classifier as KClass<*>
        kclass.isSealed
      }
      false -> false
    }
  }

  // it's common to have custom decoders for sealed classes, so this decoder should be very low priority
  override fun priority(): Int = Integer.MIN_VALUE + 100

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Any> {

    val kclass = type.classifier as KClass<*>

    // if we have no subclasses then that is an error of course
    if (kclass.sealedSubclasses.isEmpty()) return ConfigFailure.SealedClassWithoutImpls(kclass).invalid()

    return when (val field = context.sealedTypeDiscriminatorField) {
      null -> deriveInstance(node, type, context)
      else -> useDiscriminator(field, node, type, context)
    }
  }

  private fun useDiscriminator(field: String, node: Node, type: KType, context: DecoderContext): ConfigResult<Any> {
    val kclass = type.classifier as KClass<*>
    val subclasses = kclass.sealedSubclasses

    // when explicitly specifying subtypes, we must have a map type containing the disriminator field,
    // or a string type referencing an object instance
    return when (node) {
      is StringNode -> {
        val referencedName = node.value
        val subtype = subclasses.find { it.java.simpleName == referencedName }?.objectInstance
        subtype?.valid() ?: ConfigFailure.NoSealedClassObjectSubtype(kclass, referencedName).invalid()
      }
      is MapNode -> {
        when (val discriminatorField = node[field]) {
          is StringNode -> {
            val subtype = subclasses.find { it.java.simpleName == discriminatorField.value }
            if (subtype == null) {
              ConfigFailure.NoSuchSealedSubtype(kclass, discriminatorField.value).invalid()
            } else {
              // check for object-ness first
              subtype.objectInstance?.valid()
              // now we know the type is not an object, we can use the data class decoder directly
                ?: DataClassDecoder().decode(node, subtype.createType(), context)
            }
          }
          else -> ConfigFailure.InvalidDiscriminatorField(kclass, field).invalid()
        }
      }
      else -> ConfigFailure.Generic("Sealed type values must be maps or strings").invalid()
    }
  }

  // to determine which sealed class to use, we can just try each in turn until one results in success
  private fun deriveInstance(node: Node, type: KType, context: DecoderContext): Validated<ConfigFailure, Any> {
    val kclass = type.classifier as KClass<*>
    val subclasses = kclass.sealedSubclasses

    return when {
      // if we have a map with no values then we can look for an object subclass,
      // but only if there is a single object subclass, otherwise we don't know which one to pick
      node is MapNode && node.size == 0 -> {
        val objects = subclasses.mapNotNull { it.objectInstance }
        when {
          objects.isEmpty() -> ConfigFailure.SealedClassWithoutObject(kclass).invalid()
          objects.size == 1 -> objects.first().valid()
          else -> ConfigFailure.SealedClassDisambiguationError(objects).invalid()
        }
      }
      else -> {

        // if we have a string node and that string is the name of an object instance,
        // we can use the object directly
        val error = if (node is StringNode) {
          val obj = subclasses.find { it.simpleName == node.value }?.objectInstance
          if (obj != null) return obj.valid() else ConfigFailure.NoSealedClassObjectSubtype(kclass, node.value)
        } else null

        val results = kclass.sealedSubclasses
          .filter { subclass ->
            subclass hasConstructorsWithArgumentsNumberLessOrEqualTo node.expectedNumberOfConstructorArguments
          }
          .sortedWith { subclass1, subclass2 ->
            (
              subclass1.numberOfMandatoryConstructorArguments.compareTo(subclass2.numberOfMandatoryConstructorArguments)
                .takeUnless { it == 0 }
                ?: subclass1.numberOfTotalConstructorArguments.compareTo(subclass2.numberOfTotalConstructorArguments)
              ) * -1
          }
          .map { DataClassDecoder().decode(node, it.createType(), context) }

        val success = results.firstOrNull { it.isValid() }
        if (success != null) return success

        val errors = results.sequence().getInvalidUnsafe() + listOfNotNull(error)
        return ConfigFailure.SealedClassSubtypeFailure(kclass, node, errors).invalid()
      }
    }
  }

  private val KClass<*>.numberOfTotalConstructorArguments
    get() = constructors.maxOfOrNull { it.numberOfTotalArguments } ?: 0

  private val KClass<*>.numberOfMandatoryConstructorArguments
    get() = constructors.maxOfOrNull { it.numberOfMandatoryArguments } ?: 0

  private infix fun KClass<*>.hasConstructorsWithArgumentsNumberLessOrEqualTo(number: Int) =
    constructors.map { it.numberOfMandatoryArguments }.any { it <= number }

  private val KFunction<*>.numberOfMandatoryArguments get() = parameters.filterNot { it.isOptional }.size
  private val KFunction<*>.numberOfTotalArguments get() = parameters.size

  private val Node.expectedNumberOfConstructorArguments get() = size.takeIf { it > 0 } ?: 1
}
