package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import kotlin.reflect.KType

class RegexDecoder : NonNullableDecoder<Regex> {

  override fun supports(type: KType): Boolean = type.classifier == Regex::class

  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Regex> = when (val v = node.value) {
    is Value.StringNode -> v.value.toRegex().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
