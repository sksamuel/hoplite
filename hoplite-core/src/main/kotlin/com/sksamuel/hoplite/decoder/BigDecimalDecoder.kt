package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigDecimal
import kotlin.reflect.KType

class BigDecimalDecoder : BasicDecoder<BigDecimal> {
  override fun supports(type: KType): Boolean = type.classifier == BigDecimal::class
  override fun decode(node: Node, path: String): ConfigResult<BigDecimal> = when (node) {
    is StringNode -> Try { node.value.toDouble().toBigDecimal() }.toValidated {
      ThrowableFailure(it)
    }.toValidatedNel()
    is LongNode -> node.value.toBigDecimal().validNel()
    is DoubleNode -> node.value.toBigDecimal().validNel()
    else -> ConfigFailure.conversionFailure<Short>(node).invalidNel()
  }
}
