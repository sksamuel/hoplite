package com.sksamuel.hoplite.decoder

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.NullForNonNull
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass

class DataClassDecoder<T : Any>(private val klass: KClass<T>,
                                private val registry: DecoderRegistry) : Decoder<T> {

  override fun convert(value: Value): ConfigResult<T> {

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      when (val vv = value.atKey(param.name!!)) {
        is NullValue ->
          if (param.type.isMarkedNullable) null.validNel() else NullForNonNull(vv, param.name ?: "<none>").invalidNel()
        else -> registry.decoder(param.type).flatMap { it.convert(vv) }
      }
    }.sequence()

    return args.map {
      klass.constructors.first().call(*it.toTypedArray())
    }
  }
}