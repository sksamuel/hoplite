package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigInteger
import kotlin.reflect.KType

class BigIntegerDecoder : BasicDecoder<BigInteger> {
  override fun supports(type: KType): Boolean = type.classifier == BigInteger::class
  override fun decode(node: Node): ConfigResult<BigInteger> = when (node) {
    is StringNode -> Try { node.value.toLong().toBigInteger() }.toValidated {
      ThrowableFailure(it, null)
    }.toValidatedNel()
    is LongNode -> node.value.toBigInteger().validNel()
    else -> ConfigFailure.conversionFailure<Short>(node).invalidNel()
  }
}