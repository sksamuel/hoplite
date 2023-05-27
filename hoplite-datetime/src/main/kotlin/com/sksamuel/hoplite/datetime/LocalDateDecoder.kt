package com.sksamuel.hoplite.datetime

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NonNullableLeafDecoder
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.fp.invalid
import kotlinx.datetime.LocalDate
import kotlin.reflect.KType

class LocalDateDecoder : NonNullableLeafDecoder<LocalDate> {
  override fun supports(type: KType): Boolean = type.classifier == LocalDate::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext
  ): ConfigResult<LocalDate> = when (node) {
    is StringNode -> runCatching { LocalDate.parse(node.value) }.toValidated {
       ConfigFailure.DecodeError(node, type)
    }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
