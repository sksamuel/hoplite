package com.sksamuel.hoplite.decoder

import arrow.core.invalid
import arrow.core.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import kotlin.reflect.KType

class RegexDecoder : NonNullableDecoder<Regex> {

  override fun supports(type: KType): Boolean = type.classifier == Regex::class

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Regex> = when (node) {
    is StringNode -> node.value.toRegex().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
