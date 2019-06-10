package com.sksamuel.hoplite.converter

import arrow.data.invalidNel
import arrow.data.valid
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.parseDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeConverterProvider : ParameterizedConverterProvider<LocalDateTime>() {
  override fun converter(): Converter<LocalDateTime> = object : Converter<LocalDateTime> {
    override fun apply(cursor: Cursor): ConfigResult<LocalDateTime> =
        when (val v = cursor.value()) {
          is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
          is LocalDateTime -> v.validNel()
          is Long -> LocalDateTime.ofInstant(Instant.ofEpochMilli(v), ZoneOffset.UTC).validNel()
          is String -> LocalDateTime.parse(v).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}

class LocalDateConverterProvider : ParameterizedConverterProvider<LocalDate>() {
  override fun converter(): Converter<LocalDate> = object : Converter<LocalDate> {
    override fun apply(cursor: Cursor): ConfigResult<LocalDate> =
        when (val v = cursor.value()) {
          is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).toLocalDate().valid()
          is LocalDate -> v.validNel()
          is String -> LocalDate.parse(v).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}

class DurationConverterProvider : ParameterizedConverterProvider<Duration>() {
  override fun converter(): Converter<Duration> = object : Converter<Duration> {
    override fun apply(cursor: Cursor): ConfigResult<Duration> =
        when (val v = cursor.value()) {
          is String -> parseDuration(v)
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}