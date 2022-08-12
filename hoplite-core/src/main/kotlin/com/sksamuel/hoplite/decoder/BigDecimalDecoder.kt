package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.fp.result.toValidated
import java.math.BigDecimal
import kotlin.reflect.KType

class BigDecimalDecoder : NonNullableLeafDecoder<BigDecimal> {
  override fun supports(type: KType): Boolean = type.classifier == BigDecimal::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              context: DecoderContext): ConfigResult<BigDecimal> = when (node) {
    is StringNode -> runCatching { node.value.toDouble().toBigDecimal() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toBigDecimal().valid()
    is DoubleNode -> node.value.toBigDecimal().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
