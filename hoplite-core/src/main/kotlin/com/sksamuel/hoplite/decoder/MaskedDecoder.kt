package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import kotlin.reflect.KType

class MaskedDecoder : NonNullableDecoder<Masked> {
  override fun supports(type: KType): Boolean = type.classifier == Masked::class
  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Masked> = when (val v = node.value) {
    is Value.StringNode -> Masked(v.value).valid()
    is Value.LongNode -> Masked(v.value.toString()).valid()
    is Value.DoubleNode -> Masked(v.value.toString()).valid()
    else -> ConfigFailure.DecodeError(node, type).invalid()
  }
}
