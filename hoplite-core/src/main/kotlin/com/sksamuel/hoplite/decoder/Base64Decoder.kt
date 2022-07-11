package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.nio.ByteBuffer
import kotlin.reflect.KType

data class Base64(val value: ByteBuffer)

class Base64Decoder : Decoder<Base64> {
  override fun supports(type: KType): Boolean = type.classifier == Base64::class
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Base64> {
    return when (node) {
      is StringNode -> runCatching {
        java.util.Base64.getDecoder().decode(node.value)
      }.fold(
        { Base64(ByteBuffer.wrap(it)).valid() },
        { ConfigFailure.DecodeError(node, type).invalid() })
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
