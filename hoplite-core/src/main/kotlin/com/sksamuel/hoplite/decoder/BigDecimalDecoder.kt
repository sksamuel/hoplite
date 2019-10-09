package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigDecimal
import kotlin.reflect.KType

class BigDecimalDecoder : NonNullableDecoder<BigDecimal> {
  override fun supports(type: KType): Boolean = type.classifier == BigDecimal::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<BigDecimal> = when (node) {
    is StringValue -> Try { node.value.toDouble().toBigDecimal() }.toValidated { ThrowableFailure(it) }
    is LongValue -> node.value.toBigDecimal().valid()
    is DoubleValue -> node.value.toBigDecimal().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
