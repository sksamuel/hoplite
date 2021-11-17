package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecodeMode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ParameterMapper
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

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>
    if (klass.constructors.isEmpty()) {
      val instance = klass.objectInstance
      if (instance != null && node is StringNode && node.value == type.simpleName.substringAfter("$")) {
        return instance.valid()
      }
      return ConfigFailure.DataClassWithoutConstructor(klass).invalid()
    }

    data class Arg(
      val parameter: KParameter,
      val configName: String, // the config value name that was used
      val value: Any?,
    )

    data class Constructor(
      val constructor: KFunction<Any>,
      val args: List<Arg>,
    )

    val constructors = klass.constructors.map { constructor ->

      // try for the value type
      // we have a special case, which is a data class with a single field with the name 'value'.
      // we call this a "value type" and we can instantiate a value directly into this data class
      // without needing nested config, if the node is a primitive type
      if (constructor.parameters.size == 1 && constructor.parameters[0].name == "value" && node is PrimitiveNode) {
        return context.decoder(constructor.parameters[0])
          .flatMap { it.decode(node, constructor.parameters[0].type, context) }
          .map { constructor.parameters[0] to it }
          .mapInvalid { ConfigFailure.ValueTypeFailure(klass, constructor.parameters[0], it) }
          .flatMap { construct(type, constructor, mapOf(it)) }
      }

      // create a map of parameter to value. in the case of defaults, we skip the parameter completely.
      val args: ValidatedNel<ConfigFailure, List<Arg>> = constructor.parameters.mapNotNull { param ->

        var name = "<<undefined>>"

        // try each parameter mapper in turn to find the node
        val n = context.paramMappers.fold<ParameterMapper, Node>(Undefined) { n, mapper ->
          if (n.isDefined) n else {
            name = mapper.map(param)
            node.atKey(name)
          }
        }

        when {
          // if we have no value for this parameter at all, and it is optional we can skip it, and
          // kotlin will use the default
          param.isOptional && n is Undefined -> null
          else -> context.decoder(param)
            .flatMap { it.decode(n, param.type, context) }
            .map { Arg(param, name, it) }
            .mapInvalid { ConfigFailure.ParamFailure(param, it) }
        }
      }.sequence()

      args.map { Constructor(constructor, it) }
    }

    // see if one of the constructors worked
    val firstValidOrLastInvalidArgs: Validated<NonEmptyList<ConfigFailure>, Constructor> = constructors
      .find { it is Validated.Valid } ?: constructors.last { it is Validated.Invalid }

    return firstValidOrLastInvalidArgs.fold(
      // if invalid, we wrap in an error containing each individual error
      { ConfigFailure.DataClassFieldErrors(it, type, node.pos).invalid() },
      { constructor ->

        // in strict mode we throw an error if not all config values were used for the class
        if (node is MapNode && context.mode == DecodeMode.Strict && constructor.args.size != node.size) {
          val unusedValues = node.map.keys.minus(constructor.args.map { it.configName }.toSet())
          ConfigFailure.UnusedConfigValues(unusedValues.toList()).invalid()
        } else {
          construct(
            type = type,
            constructor = constructor.constructor,
            args = constructor.args.associate { it.parameter to it.value },
          )
        }
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
