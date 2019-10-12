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
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.parseDuration
import com.sksamuel.hoplite.parsePeriod
import java.time.Period

class LocalDateTimeDecoder : NonNullableDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<LocalDateTime> = when (val v = node.value) {
    is Value.LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(v.value), ZoneOffset.UTC).valid()
    is Value.StringNode ->
      Try { LocalDateTime.parse(v.value, DateTimeFormatter.ISO_DATE_TIME).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LocalDateDecoder : NonNullableDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<LocalDate> = when (val v = node.value) {
    is Value.StringNode ->
      Try { LocalDate.parse(v.value).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class DurationDecoder : NonNullableDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Duration> = when (val v = node.value) {
    is Value.StringNode -> parseDuration(v.value).leftMap { ConfigFailure.DecodeError(node, type) }
    is Value.LongNode -> Duration.ofMillis(v.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class InstantDecoder : NonNullableDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Instant> = when (val v = node.value) {
    is Value.StringNode -> Try { Instant.ofEpochMilli(v.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is Value.LongNode -> Instant.ofEpochMilli(v.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearDecoder: NonNullableDecoder<Year> {
  override fun supports(type: KType): Boolean = type.classifier == java.time.Year::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Year> = when (val v = node.value) {
    is Value.StringNode -> Try { Year.of(v.value.toInt()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is Value.LongNode -> Year.of(v.value.toInt()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class JavaUtilDateDecoder : NonNullableDecoder<Date> {
  override fun supports(type: KType): Boolean = type.classifier == java.util.Date::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Date> = when (val v = node.value) {
    is Value.StringNode -> Try { Date(v.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is Value.LongNode -> Date(v.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class YearMonthDecoder : NonNullableDecoder<YearMonth> {
  override fun supports(type: KType): Boolean = type.classifier == YearMonth::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<YearMonth> = when (val v = node.value) {
    is Value.StringNode -> Try { YearMonth.parse(v.value).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class PeriodDecoder : NonNullableDecoder<Period> {
  override fun supports(type: KType): Boolean = type.classifier == Period::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Period> = when (val v = node.value) {
    is Value.StringNode -> parsePeriod(v.value).leftMap { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class SqlTimestampDecoder : NonNullableDecoder<java.sql.Timestamp> {
  override fun supports(type: KType): Boolean = type.classifier == java.sql.Timestamp::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<java.sql.Timestamp> = when (val v = node.value) {
    is Value.StringNode -> Try { java.sql.Timestamp(v.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, type).invalid() }
    is Value.LongNode -> java.sql.Timestamp(v.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
