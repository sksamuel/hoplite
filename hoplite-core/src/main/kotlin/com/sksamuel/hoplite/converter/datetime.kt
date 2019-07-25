package com.sksamuel.hoplite.converter

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

class LocalDateTimeConverterProvider : ParameterizedConverterProvider<LocalDateTime>() {
  override fun converter(): Converter<LocalDateTime> = object : Converter<LocalDateTime> {
    override fun apply(value: Value): ConfigResult<LocalDateTime> =
        when (value) {
          //is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
          //is LocalDateTime -> v.validNel()
          is LongValue -> LocalDateTime.ofInstant(Instant.ofEpochMilli(value.value), ZoneOffset.UTC).validNel()
          is StringValue -> LocalDateTime.parse(value.value, DateTimeFormatter.ISO_DATE_TIME).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
        }
  }
}

class LocalDateConverterProvider : ParameterizedConverterProvider<LocalDate>() {
  override fun converter(): Converter<LocalDate> = object : Converter<LocalDate> {
    override fun apply(value: Value): ConfigResult<LocalDate> =
        when (value) {
          //    is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).toLocalDate().valid()
          //    is LocalDate -> v.validNel()
          is StringValue -> LocalDate.parse(value.value).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
        }
  }
}

class DurationConverterProvider : ParameterizedConverterProvider<Duration>() {
  override fun converter(): Converter<Duration> = object : Converter<Duration> {
    override fun apply(value: Value): ConfigResult<Duration> =
        when (value) {
          is StringValue -> parseDuration(value.value)
          else -> ConfigFailure.conversionFailure<LocalDateTime>(value).invalidNel()
        }
  }
}