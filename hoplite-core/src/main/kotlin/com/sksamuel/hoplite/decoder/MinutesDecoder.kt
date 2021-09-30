package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class MinutesDecoder : NonNullableLeafDecoder<Minutes> {

  override fun supports(type: KType): Boolean = type.classifier == Minutes::class
  override fun safeLeafDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Minutes> = when (node) {
    is StringNode -> Try { Minutes(node.value.toLong()) }.toValidated { ThrowableFailure(it) }
    is LongNode -> Minutes(node.value).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

data class Minutes(val value: Long)

@ExperimentalTime
fun Minutes.duration() = Duration.minutes(value)
