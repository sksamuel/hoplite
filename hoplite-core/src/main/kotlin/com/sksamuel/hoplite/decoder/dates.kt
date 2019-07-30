package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.core.getOrElse
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
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
    is LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).valid()
    is StringNode ->
      Try { LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, path, type).invalid() }
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}

class LocalDateDecoder : NonNullableDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<LocalDate> = when (node) {
    is StringNode ->
      Try { LocalDate.parse(node.value).valid() }
        .getOrElse { ConfigFailure.DecodeError(node, path, type).invalid() }
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}

class DurationDecoder : NonNullableDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Duration> = when (node) {
    is StringNode -> parseDuration(node.value).leftMap { ConfigFailure.DecodeError(node, path, type) }
    is LongNode -> Duration.ofMillis(node.value).valid()
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}

class InstantDecoder : NonNullableDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Instant> = when (node) {
    is StringNode -> Try { Instant.ofEpochMilli(node.value.toLong()).valid() }
      .getOrElse { ConfigFailure.DecodeError(node, path, type).invalid() }
    is LongNode -> Instant.ofEpochMilli(node.value).valid()
    else -> ConfigFailure.DecodeError(node, path, type).invalid()
  }
}
