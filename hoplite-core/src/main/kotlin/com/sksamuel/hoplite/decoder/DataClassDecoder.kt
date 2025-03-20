package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.ValidatedNel
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.isDefined
import com.sksamuel.hoplite.simpleName
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class DataClassDecoder : NullHandlingDecoder<Any> {

  override fun supports(type: KType): Boolean {
    return when (type.classifier is KClass<*>) {
      true -> {
        val kclass = type.classifier as KClass<*>
        // we don't handle sealed in here as we have a custom sealed decoder
        kclass.isData && !kclass.isSealed && !kclass.isInline()
      }
      false -> false
    }
  }

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<Any> =
    // unlike most NullHandlingDecoders, we defer null handling to see if constructors with default args apply
    safeDecode(node, type, context)

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<Any> {

    val kclass = type.classifier as KClass<*>
    if (kclass.constructors.isEmpty()) {
      val instance = kclass.objectInstance
      if (instance != null && node is StringNode && node.value == type.simpleName.substringAfter("$")) {
        return instance.valid()
      }
      return ConfigFailure.DataClassWithoutConstructor(kclass).invalid()
    }

    data class Arg(
      val parameter: KParameter,
      val configName: String, // the config value name that was used, as determined by param mappers
      val value: Any?,
      val node: Node, // the resolved node that provided the value
    )

    data class Constructor(
      val constructor: KFunction<Any>,
      val args: List<Arg>
    )

    val constructors = kclass.constructors.map { constructor ->

      // try for the value type
      // we have a special case, which is a data class with a single field with the name 'value'.
      // we call this a "value type" and we can instantiate a value directly into this data class
      // without needing nested config, if the node is a primitive type
      if (constructor.parameters.size == 1 && constructor.parameters[0].name == "value" && node is PrimitiveNode) {
        return context.decoder(constructor.parameters[0])
          .flatMap { it.decode(node, constructor.parameters[0].type, context) }
          .map { constructor.parameters[0] to it }
          .mapInvalid { ConfigFailure.ValueTypeFailure(kclass, constructor.parameters[0], it) }
          .flatMap { construct(type, constructor, mapOf(it)) }
      }

      // create a map of parameter to value. in the case of defaults, we skip the parameter completely.
      val args: ValidatedNel<ConfigFailure, List<Arg>> = constructor.parameters.mapNotNull { param ->

        var usedName = "<<undefined>>"

        // use parameter mappers to retrieve alternative names, then try each one in turn
        // until we find one that is defined
        val names = context.paramMappers.flatMap { it.map(param, constructor, kclass) }

        fun nameLookup(name: String): Node {
          var atKey = node.atKey(name)
          // check if the node has a key matching a transformed path element
          if (atKey is Undefined) atKey = node.atKey(
            context.nodeTransformers.fold(name) { n, transformer -> transformer.transformPathElement(n) }
          )
          // also check the source key, as parameter mappers may be referring to the source name
          if (atKey is Undefined) atKey = node.atSourceKey(name)
          return atKey
        }

        // every alternative name is marked as used so strict can detect that overrides were 'used' too.
        names.forEach {
          context.usedPaths.add(nameLookup(it).path)
        }

        val n = names.fold<String, Node>(Undefined) { n, name ->
          if (n.isDefined) n else {
            usedName = name
            nameLookup(name)
          }
        }

        when {
          // if we have no value for this parameter at all, and it is optional we can skip it, and
          // kotlin will use the default
          param.isOptional && n is Undefined -> null
          else ->
            context.decoder(param).flatMap { decoder ->
              runBlocking {
                context.resolvers.resolve(n, param.name ?: "unknown", kclass, context).flatMap { resolvedNode ->
                  decoder.decode(resolvedNode, param.type, context).map { decoded ->
                    Arg(param, usedName, decoded, n)
                  }
                }
              }
            }.mapInvalid { ConfigFailure.ParamFailure(param, it) }
        }
      }.sequence()

      args.onSuccess { argList -> argList.forEach { context.used(it.node, it.parameter.type, it.value) } }
      args.map { Constructor(constructor, it) }
    }

    // see if one of the constructors worked
    val firstValidOrLastInvalidArgs: Validated<NonEmptyList<ConfigFailure>, Constructor> = constructors
      .find { it is Validated.Valid } ?: constructors.last { it is Validated.Invalid }

    return firstValidOrLastInvalidArgs.fold(
      {
        when (node) {
          // for Undefined and NullNode, fall back to the NullHandlingDecoder errors
          is Undefined, is NullNode -> super.decode(node, type, context)
          // otherwise, wrap in an error containing each individual error
          else -> ConfigFailure.DataClassFieldErrors(it, type, node.pos).invalid()
        }
      },
      { constructor ->
        construct(
          type = type,
          constructor = constructor.constructor,
          args = constructor.args.associate { it.parameter to it.value }
        )
      }
    )
  }

  private fun <A> construct(
    type: KType,
    constructor: KFunction<A>,
    args: Map<KParameter, Any?>
  ): ConfigResult<A> {
    return try {
      constructor.callBy(args).valid()
    } catch (e: InvocationTargetException) {
      ConfigFailure.InvalidConstructorParameters(type, constructor, args, e.cause ?: e).invalid()
    } catch (e: IllegalArgumentException) {
      ConfigFailure.InvalidConstructorParameters(type, constructor, args, e.cause ?: e).invalid()
    }
  }
}
