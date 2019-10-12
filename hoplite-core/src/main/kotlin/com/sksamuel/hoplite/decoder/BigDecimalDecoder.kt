package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigDecimal
import kotlin.reflect.KType

class BigDecimalDecoder : NonNullableDecoder<BigDecimal> {
  override fun supports(type: KType): Boolean = type.classifier == BigDecimal::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<BigDecimal> = when (val v = node.value) {
    is Value.StringNode -> Try { v.value.toDouble().toBigDecimal() }.toValidated { ThrowableFailure(it) }
    is Value.LongNode -> v.value.toBigDecimal().valid()
    is Value.DoubleNode -> v.value.toBigDecimal().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
