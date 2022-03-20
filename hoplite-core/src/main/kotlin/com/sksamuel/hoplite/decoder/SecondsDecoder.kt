package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.time.Duration.Companion.seconds

class SecondsDecoder : NonNullableLeafDecoder<Seconds> {

  override fun supports(type: KType): Boolean = type.classifier == Seconds::class
  override fun safeLeafDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Seconds> = when (node) {
    is StringNode -> runCatching { Seconds(node.value.toLong()) }.toValidated { ThrowableFailure(it) }
    is LongNode -> Seconds(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

data class Seconds(val value: Long)

fun Seconds.duration() = value.seconds
