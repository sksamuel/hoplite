package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import kotlin.reflect.KType

class RegexDecoder : NullHandlingDecoder<Regex> {

  override fun supports(type: KType): Boolean = type.classifier == Regex::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Regex> = when (node) {
    is StringNode -> node.value.toRegex().valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
