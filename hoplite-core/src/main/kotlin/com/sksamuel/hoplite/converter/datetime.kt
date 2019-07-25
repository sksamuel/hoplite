package com.sksamuel.hoplite.converter

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.parseDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LocalDateTimeConverterProvider : ParameterizedConverterProvider<LocalDateTime>() {
  override fun converter(): Converter<LocalDateTime> = object : Converter<LocalDateTime> {
    override fun apply(cursor: Cursor): ConfigResult<LocalDateTime> =
        when (val v = cursor.value()) {
          //is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
          //is LocalDateTime -> v.validNel()
          is LongValue -> LocalDateTime.ofInstant(Instant.ofEpochMilli(v.value), ZoneOffset.UTC).validNel()
          is StringValue -> LocalDateTime.parse(v.value, DateTimeFormatter.ISO_DATE_TIME).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}

class LocalDateConverterProvider : ParameterizedConverterProvider<LocalDate>() {
  override fun converter(): Converter<LocalDate> = object : Converter<LocalDate> {
    override fun apply(cursor: Cursor): ConfigResult<LocalDate> =
        when (val v = cursor.value()) {
          //    is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).toLocalDate().valid()
          //    is LocalDate -> v.validNel()
          is StringValue -> LocalDate.parse(v.value).validNel()
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}

class DurationConverterProvider : ParameterizedConverterProvider<Duration>() {
  override fun converter(): Converter<Duration> = object : Converter<Duration> {
    override fun apply(cursor: Cursor): ConfigResult<Duration> =
        when (val v = cursor.value()) {
          is StringValue -> parseDuration(v.value)
          else -> ConfigFailure.conversionFailure<LocalDateTime>(v).invalidNel()
        }
  }
}