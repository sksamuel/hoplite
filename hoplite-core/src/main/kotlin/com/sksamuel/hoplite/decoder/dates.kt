package com.sksamuel.hoplite.decoder

import kotlin.reflect.KType

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.Year
import java.time.YearMonth
import java.util.Date

import arrow.core.Try
import arrow.core.getOrElse
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.parseDuration
import com.sksamuel.hoplite.parsePeriod
import java.time.Period

class LocalDateTimeDecoder : NonNullableDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<LocalDateTime> = when (node) {
    is LongValue -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).valid()
    is StringValue ->
      Try { LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LocalDateDecoder : NonNullableDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<LocalDate> = when (node) {
    is StringValue ->
      Try { LocalDate.parse(node.value).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DurationDecoder : NonNullableDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Duration> = when (node) {
    is StringValue -> parseDuration(node.value).leftMap { ConfigFailure.DecodeError(node, type) }
    is LongValue -> Duration.ofMillis(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class InstantDecoder : NonNullableDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Instant> = when (node) {
    is StringValue -> Try { Instant.ofEpochMilli(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongValue -> Instant.ofEpochMilli(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearDecoder: NonNullableDecoder<Year> {
  override fun supports(type: KType): Boolean = type.classifier == java.time.Year::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Year> = when(node) {
    is StringValue -> Try { Year.of(node.value.toInt()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongValue -> Year.of(node.value.toInt()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class JavaUtilDateDecoder : NonNullableDecoder<java.util.Date> {
  override fun supports(type: KType): Boolean = type.classifier == java.util.Date::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Date> = when (node) {
    is StringValue -> Try { java.util.Date(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongValue -> java.util.Date(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearMonthDecoder : NonNullableDecoder<YearMonth> {
  override fun supports(type: KType): Boolean = type.classifier == YearMonth::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<YearMonth> = when (node) {
    is StringValue -> Try { YearMonth.parse(node.value).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PeriodDecoder : NonNullableDecoder<Period> {
  override fun supports(type: KType): Boolean = type.classifier == Period::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Period> = when (node) {
    is StringValue -> parsePeriod(node.value).leftMap { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class SqlTimestampDecoder : NonNullableDecoder<java.sql.Timestamp> {
  override fun supports(type: KType): Boolean = type.classifier == java.sql.Timestamp::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<java.sql.Timestamp> = when (node) {
    is StringValue -> Try { java.sql.Timestamp(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongValue -> java.sql.Timestamp(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
