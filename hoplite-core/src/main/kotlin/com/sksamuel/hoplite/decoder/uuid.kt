package com.sksamuel.hoplite.decoder

import arrow.core.Try
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.toValidated
import java.util.*
import kotlin.reflect.KType

class UUIDDecoder : NonNullableDecoder<UUID> {
  override fun supports(type: KType): Boolean = type.classifier == UUID::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<UUID> = when (val v = node.value) {
    is Value.StringNode ->
      Try { UUID.fromString(v.value) }
        .toValidated { ConfigFailure.DecodeError(node, type) }
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
