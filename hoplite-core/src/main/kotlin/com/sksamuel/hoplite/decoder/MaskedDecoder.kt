package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import kotlin.reflect.KType

class MaskedDecoder : BasicDecoder<Masked> {
  override fun supports(type: KType): Boolean = type.classifier == Masked::class
  override fun decode(node: Node, path: String): ConfigResult<Masked> = when (node) {
    is StringNode -> Masked(node.value).validNel()
    is LongNode -> Masked(node.value.toString()).validNel()
    is DoubleNode -> Masked(node.value.toString()).validNel()
    else -> ConfigFailure.conversionFailure<Masked>(node).invalidNel()
  }
}
