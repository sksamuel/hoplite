package com.sksamuel.hoplite.decoder

import kotlin.reflect.KType

interface NonNullableLeafDecoder<T> : NullHandlingDecoder<T> {

  fun safeLeafDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T>

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> {
    return when (node) {
      is MapNode -> decode(node.value, type, context)
      else -> safeLeafDecode(node, type, context)
    }
  }
}
