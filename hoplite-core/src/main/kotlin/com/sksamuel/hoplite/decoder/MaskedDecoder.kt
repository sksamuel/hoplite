package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import kotlin.reflect.KType

class MaskedDecoder : NonNullableDecoder<Masked> {
  override fun supports(type: KType): Boolean = type.classifier == Masked::class
  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Masked> = when (node) {
    is StringValue -> Masked(node.value).valid()
    is LongValue -> Masked(node.value.toString()).valid()
    is DoubleValue -> Masked(node.value.toString()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
