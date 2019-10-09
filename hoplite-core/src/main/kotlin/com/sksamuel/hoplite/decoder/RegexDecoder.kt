package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import kotlin.reflect.KType

class RegexDecoder : NonNullableDecoder<Regex> {

  override fun supports(type: KType): Boolean = type.classifier == Regex::class

  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Regex> = when (node) {
    is StringValue -> node.value.toRegex().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
