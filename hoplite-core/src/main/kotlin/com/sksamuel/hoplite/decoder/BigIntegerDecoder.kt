package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.ThrowableFailure
import com.sksamuel.hoplite.arrow.toValidated
import java.math.BigInteger
import kotlin.reflect.KType

class BigIntegerDecoder : NonNullableDecoder<BigInteger> {
  override fun supports(type: KType): Boolean = type.classifier == BigInteger::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<BigInteger> = when (node) {
    is StringValue -> Try { node.value.toLong().toBigInteger() }.toValidated { ThrowableFailure(it) }
    is LongValue -> node.value.toBigInteger().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}

