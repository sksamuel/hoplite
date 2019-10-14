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
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.parseDuration
import com.sksamuel.hoplite.parsePeriod
import java.time.Period

class LocalDateTimeDecoder : NonNullableLeafDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<LocalDateTime> = when (node) {
    is LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).valid()
    is StringNode ->
      Try { LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LocalDateDecoder : NonNullableLeafDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<LocalDate> = when (node) {
    is StringNode ->
      Try { LocalDate.parse(node.value).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DurationDecoder : NonNullableLeafDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Duration> = when (node) {
    is StringNode -> parseDuration(node.value).leftMap { ConfigFailure.DecodeError(node, type) }
    is LongNode -> Duration.ofMillis(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class InstantDecoder : NonNullableLeafDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Instant> = when (node) {
    is StringNode -> Try { Instant.ofEpochMilli(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongNode -> Instant.ofEpochMilli(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearDecoder : NonNullableLeafDecoder<Year> {
  override fun supports(type: KType): Boolean = type.classifier == java.time.Year::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Year> = when (node) {
    is StringNode -> Try { Year.of(node.value.toInt()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongNode -> Year.of(node.value.toInt()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class JavaUtilDateDecoder : NonNullableLeafDecoder<Date> {
  override fun supports(type: KType): Boolean = type.classifier == java.util.Date::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Date> = when (node) {
    is StringNode -> Try { Date(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongNode -> Date(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearMonthDecoder : NonNullableLeafDecoder<YearMonth> {
  override fun supports(type: KType): Boolean = type.classifier == YearMonth::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<YearMonth> = when (node) {
    is StringNode -> Try { YearMonth.parse(node.value).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PeriodDecoder : NonNullableLeafDecoder<Period> {
  override fun supports(type: KType): Boolean = type.classifier == Period::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<Period> = when (node) {
    is StringNode -> parsePeriod(node.value).leftMap { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class SqlTimestampDecoder : NonNullableLeafDecoder<java.sql.Timestamp> {
  override fun supports(type: KType): Boolean = type.classifier == java.sql.Timestamp::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<java.sql.Timestamp> = when (node) {
    is StringNode -> Try { java.sql.Timestamp(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is LongNode -> java.sql.Timestamp(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
