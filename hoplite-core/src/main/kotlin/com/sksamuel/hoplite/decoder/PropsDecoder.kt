package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.util.Properties
import kotlin.reflect.KType

class PropsDecoder : NullHandlingDecoder<Properties> {

  override fun supports(type: KType): Boolean =
    type.classifier == Properties::class

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<Properties> {
    return when (node) {
      is MapNode -> {
        val props = Properties()
        node.map.map {
          when (val v = it.value) {
            is StringNode -> props[it.key] = v.value
            is LongNode -> props[it.key] = v.value
            is DoubleNode -> props[it.key] = v.value
            is BooleanNode -> props[it.key] = v.value
            else -> Unit
          }
        }
        props.valid()
      }
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
