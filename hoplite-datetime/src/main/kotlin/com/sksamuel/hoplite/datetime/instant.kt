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
import kotlin.reflect.KType

class InstantDecoder : NonNullableLeafDecoder<Instant> {
  override fun supports(type: KType): Boolean = type.classifier == Instant::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<Instant> = when (node) {
    is StringNode -> runCatching { Instant.fromEpochMilliseconds(node.value.toLong()) }.toValidated {
      ConfigFailure.DecodeError(node, type)
    }
    is LongNode -> Instant.fromEpochMilliseconds(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
