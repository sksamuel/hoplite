package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

class SecretDecoder : NullHandlingDecoder<Secret> {

  override fun supports(type: KType): Boolean = type.classifier == Secret::class

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Secret> {
    return when (node) {
      is StringNode -> Secret(node.value).valid()
      is LongNode -> Secret(node.value.toString()).valid()
      is DoubleNode -> Secret(node.value.toString()).valid()
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
