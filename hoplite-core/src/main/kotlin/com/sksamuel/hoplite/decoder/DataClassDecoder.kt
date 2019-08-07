package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.UndefinedNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>

    val constructor = klass.constructors.first()

    val args: ValidatedNel<ConfigFailure.ParamFailure, List<Pair<KParameter, Any?>>> = constructor.parameters.map { param ->
      val paramName = param.name ?: "<anon>"
      val n = node.atKey(paramName)

      if (param.isOptional && n is UndefinedNode) {
        (param to null).valid() // skip this parameter and let the default value be filled in
      } else {
        registry.decoder(param.type)
          .flatMap { it.decode(n, param.type, registry) }
          .map { param to it }
          .leftMap { ConfigFailure.ParamFailure(paramName, it) }
      }
    }.sequence()

    return args
      .leftMap { ConfigFailure.DataClassFieldErrors(it, type, node.pos) }
      .map { constructor.callBy(it.toMap()) }
  }
}
