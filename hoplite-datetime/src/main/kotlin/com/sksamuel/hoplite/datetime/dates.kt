package com.sksamuel.hoplite.datetime

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NonNullableLeafDecoder
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.reflect.KType

class LocalDateTimeDecoder : NonNullableLeafDecoder<LocalDateTime> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDateTime::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<LocalDateTime> = when (node) {
    is LongNode -> Instant.fromEpochMilliseconds(node.value).toLocalDateTime(TimeZone.UTC).valid()
    is StringNode ->
      runCatching { LocalDateTime.parse(node.value) }.toValidated {
        ConfigFailure.DecodeError(node, type)
      }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

class LocalDateDecoder : NonNullableLeafDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<LocalDate> = when (node) {
    is StringNode -> runCatching { LocalDate.parse(node.value) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
