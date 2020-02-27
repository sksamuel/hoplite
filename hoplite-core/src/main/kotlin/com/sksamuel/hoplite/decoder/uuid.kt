package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.Try
import java.util.*
import kotlin.reflect.KType

class UUIDDecoder : NullHandlingDecoder<UUID> {
  override fun supports(type: KType): Boolean = type.classifier == UUID::class
  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<UUID> = when (node) {
    is StringNode ->
      Try { UUID.fromString(node.value) }
        .toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
