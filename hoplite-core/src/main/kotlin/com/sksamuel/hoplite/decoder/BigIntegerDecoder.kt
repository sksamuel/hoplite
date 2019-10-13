package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigInteger
import kotlin.reflect.KType

class BigIntegerDecoder : NonNullableDecoder<BigInteger> {
  override fun supports(type: KType): Boolean = type.classifier == BigInteger::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<BigInteger> = when (node) {
    is StringNode -> Try { node.value.toLong().toBigInteger() }.toValidated { ThrowableFailure(it) }
    is LongNode -> node.value.toBigInteger().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

