package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.core.invalid
import arrow.core.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigInteger
import kotlin.reflect.KType

class BigIntegerDecoder : NonNullableLeafDecoder<BigInteger> {
  override fun supports(type: KType): Boolean = type.classifier == BigInteger::class
  override fun safeLeafDecode(node: Node,
                              type: KType,
                              registry: DecoderRegistry): ConfigResult<BigInteger> = when (node) {
    is StringNode -> Try { node.value.toLong().toBigInteger() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toBigInteger().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

