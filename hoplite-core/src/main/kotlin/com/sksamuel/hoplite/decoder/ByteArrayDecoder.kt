package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

class ByteArrayDecoder : Decoder<ByteArray> {
  override fun supports(type: KType): Boolean = type.classifier == ByteArray::class
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<ByteArray> {
    return when (node) {
      is StringNode -> node.value.encodeToByteArray().valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
