package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.plus
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KClass
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

  // it's common to have custom decoders for sealed classes, so sealed classes should be very low priority
  override fun priority(): Int = Integer.MIN_VALUE + 100

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Any> {
    // to determine which sealed class to use, we can just try each in turn until one results in success
    val kclass = type.classifier as KClass<*>
    val subclasses = kclass.sealedSubclasses

    return when {
      // if we have no subclasses then that is an error of course
      subclasses.isEmpty() -> ConfigFailure.SealedClassWithoutImpls(kclass).invalid()
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
          if (obj != null) return obj.valid() else ConfigFailure.NoSealedClassObjectSubtype(kclass, node)
        } else null

        val results = kclass.sealedSubclasses
          .sortedByDescending { subclass -> subclass.constructors.maxOfOrNull { it.parameters.size } ?: 0 }
          .map { DataClassDecoder().decode(node, it.createType(), context) }

        val success = results.firstOrNull { it.isValid() }
        if (success != null) return success

        val errors = results.sequence().getInvalidUnsafe() + listOfNotNull(error)
        return ConfigFailure.SealedClassSubtypeFailure(kclass, node, errors).invalid()
      }
    }
  }
}
