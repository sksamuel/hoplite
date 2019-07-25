package com.sksamuel.hoplite.converter

import arrow.core.Try
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.time.LocalDateTime
import java.util.*

class UUIDConverterProvider : ParameterizedConverterProvider<UUID>() {
  override fun converter(): Converter<UUID> = object : Converter<UUID> {
    override fun convert(value: Value): ConfigResult<UUID> =
        when (value) {
          is StringValue -> Try { UUID.fromString(value.value) }.toValidated { ConfigFailure("UUID could not be parsed from $value") }.toValidatedNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
        }
  }
}