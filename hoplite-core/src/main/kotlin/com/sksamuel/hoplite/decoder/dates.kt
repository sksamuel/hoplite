package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
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

class LocalDateTimeDecoder : BasicDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun decode(node: Node): ConfigResult<LocalDateTime> = when (node) {
    //is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).validNel()
    //is LocalDateTime -> v.validNel()
    is LongNode -> LocalDateTime.ofInstant(Instant.ofEpochMilli(node.value), ZoneOffset.UTC).validNel()
    is StringNode -> LocalDateTime.parse(node.value, DateTimeFormatter.ISO_DATE_TIME).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}

class LocalDateDecoder : BasicDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun decode(node: Node): ConfigResult<LocalDate> = when (node) {
    //    is java.util.Date -> LocalDateTime.ofInstant(v.toInstant(), ZoneOffset.UTC).toLocalDate().valid()
    //    is LocalDate -> v.validNel()
    is StringNode -> LocalDate.parse(node.value).validNel()
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}

class DurationDecoder : BasicDecoder<Duration> {
  override fun supports(type: KType): Boolean = type.classifier == Duration::class
  override fun decode(node: Node): ConfigResult<Duration> = when (node) {
    is StringNode -> parseDuration(node.value)
    else -> ConfigFailure.conversionFailure<LocalDateTime>(node).invalidNel()
  }
}