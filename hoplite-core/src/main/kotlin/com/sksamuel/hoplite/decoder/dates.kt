package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.core.getOrElse
import arrow.data.invalidNel
import arrow.data.valid
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.parseDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.KType

class LocalDateTimeDecoder : NonNullableDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<LocalDateTime> = when (node) {
    is LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).validNel()
    is StringNode -> LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}

class LocalDateDecoder : NonNullableDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<LocalDate> = when (node) {
    is StringNode -> LocalDate.parse(node.value).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}

class DurationDecoder : NonNullableDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Duration> = when (node) {
    is StringNode -> parseDuration(node.value)
    is LongNode -> Duration.ofMillis(node.value).valid()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}

class InstantDecoder : NonNullableDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Instant> = when (node) {
    is StringNode -> Try { Instant.ofEpochMilli(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.conversionFailure<Instant>(node).invalidNel() }
    is LongNode -> Instant.ofEpochMilli(node.value).valid()
    else -> ConfigFailure.conversionFailure<Instant>(node).invalidNel()
  }
}
