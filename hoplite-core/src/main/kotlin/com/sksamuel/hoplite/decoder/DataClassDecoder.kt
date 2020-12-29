package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ParameterMapper
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.ValidatedNel
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.isDefined
import java.lang.IllegalArgumentException
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
    if (klass.constructors.isEmpty())
      return ConfigFailure.DataClassWithoutConstructor(klass).invalid()
    val constructor = klass.constructors.first()

    // we have a special case, which is a data class with a single field with the name 'value'.
    // we call this a "value type" and we can instantiate a value directly into this data class
    // without needing nested config, if the node is a primitive type

    // try for the value type
    if (constructor.parameters.size == 1 && constructor.parameters[0].name == "value" && node is PrimitiveNode) {
      return context.decoder(constructor.parameters[0])
        .flatMap { it.decode(node, constructor.parameters[0].type, context) }
        .map { constructor.parameters[0] to it }
        .mapInvalid { ConfigFailure.ValueTypeFailure(klass, constructor.parameters[0], it) }
        .flatMap { construct(type, constructor, mapOf(it)) }
    }

    // create a map of parameter to value. in the case of defaults, we skip the parameter completely.
    val args: ValidatedNel<ConfigFailure, List<Pair<KParameter, Any?>>> = constructor.parameters.mapNotNull { param ->

      val n = context.paramMappers.fold<ParameterMapper, Node>(Undefined) { n, mapper ->
        if (n.isDefined) n else node.atKey(mapper.map(param))
      }

      when {
        // if we have no value for this parameter at all, and it is optional we can skip it, and
        // kotlin will use the default
        param.isOptional && n is Undefined -> null
        else -> context.decoder(param)
          .flatMap { it.decode(n, param.type, context) }
          .map { param to it }
          .mapInvalid { ConfigFailure.ParamFailure(param, it) }
      }
    }.sequence()

    return args
      .mapInvalid { ConfigFailure.DataClassFieldErrors(it, type, node.pos) }
      .flatMap { construct(type, constructor, it.toMap()) }
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
