package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      val paramName = param.name ?: "<anon>"
      val n = node.atKey(paramName)
      registry.decoder(param.type)
        .flatMap { it.decode(n, param.type, registry) }
        .leftMap { ConfigFailure.ParamFailure(paramName, it) }
    }.sequence()

    return args
      .leftMap { ConfigFailure.DataClassFieldErrors(it, type, node.pos) }
      .map { klass.constructors.first().call(*it.toTypedArray()) }
  }
}
