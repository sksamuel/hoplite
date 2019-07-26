package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.parseDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.KType

class DateDecoderRegistration : DecoderRegistration {
  override fun register(registry: DecoderRegistry) {
    registry.register(LocalDateTime::class, LocalDateTimeDecoder)
    registry.register(LocalDate::class, LocalDateDecoder)
    registry.register(Duration::class, DurationDecoder)
  }
}

object LocalDateTimeDecoder : Decoder<LocalDateTime> {
  override fun convert(value: Value, type: KType): ConfigResult<LocalDateTime> = when (value) {
    //is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
    //is LocalDateTime -> v.validNel()
    is LongValue -> LocalDateTime.ofInstant(Instant.ofEpochMilli(value.value), ZoneOffset.UTC).validNel()
    is StringValue -> LocalDateTime.parse(value.value, DateTimeFormatter.ISO_DATE_TIME).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
  }
}

object LocalDateDecoder : Decoder<LocalDate> {
  override fun convert(value: Value, type: KType): ConfigResult<LocalDate> = when (value) {
    //    is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).toLocalDate().valid()
    //    is LocalDate -> v.validNel()
    is StringValue -> LocalDate.parse(value.value).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
  }
}

object DurationDecoder : Decoder<Duration> {
  override fun convert(value: Value, type: KType): ConfigResult<Duration> = when (value) {
    is StringValue -> parseDuration(value.value)
    else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
  }
}