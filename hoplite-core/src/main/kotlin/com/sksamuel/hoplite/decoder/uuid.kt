package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KType

object UUIDDecoder : Decoder<UUID> {
  override fun convert(value: Value, type: KType): ConfigResult<UUID> = when (value) {
    is StringValue -> Try { UUID.fromString(value.value) }.toValidated { ConfigFailure("UUID could not be parsed from $value") }.toValidatedNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
  }
}

class UUIDDecoderRegistration : DecoderRegistration {
  override fun register(registry: DecoderRegistry) {
    registry.register(UUID::class, UUIDDecoder)
  }
}