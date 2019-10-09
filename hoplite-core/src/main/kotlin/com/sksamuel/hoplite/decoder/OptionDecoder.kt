package com.sksamuel.hoplite.decoder

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.UndefinedValue
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class OptionDecoder : Decoder<Option<*>> {

  override fun supports(type: KType): Boolean = type.classifier == Option::class

  override fun decode(value: Value,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<Option<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(value: Value, decoder: Decoder<T>): ConfigResult<Option<T>> {
      return decoder.decode(value, t, registry).map { Some(it) }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (value) {
        is UndefinedValue, is NullValue -> None.valid()
        is StringValue, is LongValue, is DoubleValue, is BooleanValue -> decode(value, decoder)
        else -> ConfigFailure.DecodeError(value, type).invalid()
      }
    }
  }
}
