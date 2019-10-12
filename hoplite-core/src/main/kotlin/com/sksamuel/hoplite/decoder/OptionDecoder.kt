package com.sksamuel.hoplite.decoder

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class OptionDecoder : Decoder<Option<*>> {

  override fun supports(type: KType): Boolean = type.classifier == Option::class

  override fun decode(node: TreeNode,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<Option<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(value: TreeNode, decoder: Decoder<T>): ConfigResult<Option<T>> {
      return decoder.decode(value, t, registry).map { Some(it) }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (node) {
        is Undefined -> None.valid()
        is PrimitiveNode -> when (node.value) {
          is Value.NullValue -> None.valid()
          is Value.StringNode, is Value.LongNode, is Value.DoubleNode, is Value.BooleanNode -> decode(node, decoder)
          else -> ConfigFailure.DecodeError(node, type).invalid()
        }
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
