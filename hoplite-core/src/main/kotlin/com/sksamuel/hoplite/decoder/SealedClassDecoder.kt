package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
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

    // when explicitly specifying subtypes, we must have a map type containing the discriminator field,
    // a string naming the subtype, or a map that has its own string value naming the subtype
    return when (node) {
      // a bare name selects an object, or a data class constructed from its defaults
      is StringNode -> decodeByName(
        node,
        MapNode(emptyMap(), node.pos, node.path, delimiter = node.delimiter, sourceKey = node.sourceKey),
        kclass,
        context
      )
      is MapNode -> {
        val discriminatorField = node[field]
        when {
          discriminatorField is StringNode -> decodeByName(
            discriminatorField,
            node,
            kclass,
            context
          ) { ConfigFailure.NoSuchSealedSubtype(kclass, it) }
          // otherwise a map with its own value names the subtype, and its children are the subtype's properties;
          // this is the idiomatic way for flat key sources to select a subtype, e.g. the env vars `FOO=Bar`
          // and `FOO_X=abc`, rather than a discriminator field
          node.value is StringNode -> decodeByName(node.value, node, kclass, context)
          else -> ConfigFailure.InvalidDiscriminatorField(kclass, field).invalid()
        }
      }
      else -> ConfigFailure.Generic("Sealed type values must be maps or strings").invalid()
    }
  }

  /**
   * Selects the subtype of [kclass] named by [name], and decodes it from [node]: an object subtype is returned
   * as is (any children of [node] are left unused), a data class subtype is decoded from the children of [node].
   * Fails with the failure [noSuchSubtypeFn] returns for the name if no subtype has that name.
   */
  private fun decodeByName(
    name: StringNode,
    node: MapNode,
    kclass: KClass<*>,
    context: DecoderContext,
    noSuchSubtypeFn: (String) -> ConfigFailure = { ConfigFailure.NoSealedClassObjectSubtype(kclass, it) },
  ): ConfigResult<Any> {
    val subtype = kclass.sealedSubclasses.findByName(name.value, context)
      ?: return noSuchSubtypeFn(name.value).invalid()
    return subtype.objectInstance?.valid() ?: DataClassDecoder().decode(node, subtype.createType(), context)
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
        // we can use the object directly. Use `.java.simpleName` so this branch matches the
        // other two name-based lookups in this file (the discriminator paths on lines 61 and
        // 67) and the `${available subclasses…}` error rendered by
        // ConfigFailure.NoSealedClassObjectSubtype — KClass.simpleName and Class.simpleName
        // happen to agree for typical nested sealed subclasses but disagree for anonymous /
        // local classes, and the inconsistency means users could see error text listing names
        // that the decoder won't actually accept.
        val error = if (node is StringNode) {
          val obj = subclasses.findByName(node.value, context)?.objectInstance
          if (obj != null) return obj.valid() else ConfigFailure.NoSealedClassObjectSubtype(kclass, node.value)
        } else null

        val results = subclasses
          .filter { subclass ->
            subclass hasConstructorsWithArgumentsNumberLessOrEqualTo node.expectedNumberOfConstructorArguments
          }
          .map { DataClassDecoder().decodeWithMatchCount(node, it.createType(), context) }

        // among the subclasses that could be decoded, prefer the one whose constructor parameters were matched by the
        // most config keys, so that a subclass is not chosen just because its unmatched parameters have defaults;
        // on a tie, the order from above applies as `maxByOrNull` returns the first maximum
        val success = results.filterIsInstance<Validated.Valid<DataClassDecoder.Decoded>>()
          .filter { if (node is MapNode && node.size == 0) true else it.value.matchedParams > 0 }
          .maxByOrNull { it.value.matchedParams }
        if (success != null) return success.value.value.valid()

        // `results` may be empty if every subclass requires more mandatory constructor
        // arguments than the supplied node provides — `hasConstructorsWithArgumentsNumberLessOrEqualTo`
        // filtered them all out. In that case `results.sequence()` was `Valid(emptyList)`,
        // and the previous `getInvalidUnsafe()` call crashed with IllegalStateException
        // ("Not an invalid instance"). Pull errors out via filter and synthesise a generic
        // failure when there's nothing else to report so the user sees a clean diagnostic
        // instead of an unrelated stack trace.
        val resultErrors = results.filterIsInstance<Validated.Invalid<ConfigFailure>>().map { it.error }
        val errors: List<ConfigFailure> = resultErrors + listOfNotNull(error)
        val nelErrors = if (errors.isEmpty())
          NonEmptyList.of<ConfigFailure>(
            ConfigFailure.Generic("No sealed subtype of ${kclass.qualifiedName} could match the supplied config")
          )
        else
          NonEmptyList.unsafe(errors)

        ConfigFailure.SealedClassSubtypeFailure(kclass, node, nelErrors).invalid()
      }
    }
  }

  /**
   * Finds the subclass whose `java.simpleName` is [name], ignoring case when
   * [com.sksamuel.hoplite.DecoderConfig.resolveTypesCaseInsensitive] is set, as [EnumDecoder] does.
   */
  private fun List<KClass<*>>.findByName(name: String, context: DecoderContext): KClass<*>? =
    find { it.java.simpleName.equals(name, ignoreCase = context.config.resolveTypesCaseInsensitive) }

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
