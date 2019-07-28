package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.UndefinedNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  private fun decode(node: NullNode, param: KParameter): ConfigResult<*> {
    return if (param.type.isMarkedNullable)
      null.validNel()
    else
      ConfigFailure.NullValueForNonNullField(node, param.name ?: "unknown").invalidNel()
  }

  private fun decode(path: String, param: KParameter): ConfigResult<*> {
    return if (param.type.isMarkedNullable)
      null.validNel()
    else
      ConfigFailure.MissingValue(path).invalidNel()
  }

  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<Any> {

    val klass = type.classifier as KClass<*>

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      val paramPath = "$path.${param.name}"
      when (val n = node.atKey(param.name!!)) {
        is UndefinedNode -> decode(paramPath, param)
        is NullNode -> decode(n, param)
        else -> registry.decoder(param.type, paramPath).flatMap { it.decode(n, param.type, registry, paramPath) }
      }
    }.sequence()

    return args.map {
      klass.constructors.first().call(*it.toTypedArray())
    }
  }
}
