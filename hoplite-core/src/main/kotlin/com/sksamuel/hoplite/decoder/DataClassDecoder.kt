package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.NullForNonNull
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass
import kotlin.reflect.KType

class DataClassDecoder : Decoder<Any> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).isData

  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<Any> {
    val klass = type.classifier as KClass<*>

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      when (val n = node.atKey(param.name!!)) {
        is NullNode -> if (param.type.isMarkedNullable) null.validNel() else NullForNonNull(n, param.name!!).invalidNel()
        else -> registry.decoder(param.type).flatMap { it.decode(n, param.type, registry) }
      }
    }.sequence()

    return args.map {
      klass.constructors.first().call(*it.toTypedArray())
    }
  }
}