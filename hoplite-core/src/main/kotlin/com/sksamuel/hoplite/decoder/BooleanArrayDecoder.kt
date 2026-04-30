package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

class BooleanArrayDecoder : Decoder<BooleanArray> {
  override fun supports(type: KType): Boolean = type.classifier == BooleanArray::class
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<BooleanArray> {
    return when (node) {
      is StringNode -> runCatching { node.value.split(",").map { it.trim().toBooleanStrict() }.toBooleanArray() }
        .fold({ it.valid() }, { ConfigFailure.DecodeError(node, type).invalid() })
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
