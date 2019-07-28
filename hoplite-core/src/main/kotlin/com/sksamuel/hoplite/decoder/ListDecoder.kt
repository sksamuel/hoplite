package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class ListDecoder : Decoder<List<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(List::class.starProjectedType)

  override fun decode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<List<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(node: ListNode, decoder: Decoder<T>): ConfigResult<List<T>> {
      return node.elements.map { decoder.decode(it, t, registry, path) }.sequence()
    }

    fun <T> decode(node: StringNode, decoder: Decoder<T>): ConfigResult<List<T>> {
      val tokens = node.value.split(",").map { it.trim() }
      return tokens.map { decoder.decode(StringNode(it, node.pos, node.dotpath), t, registry, path) }.sequence()
    }

    return registry.decoder(t, path).flatMap { decoder ->
      when (node) {
        is ListNode -> decode(node, decoder)
        is StringNode -> decode(node, decoder)
        else -> ConfigFailure.UnsupportedListType(node, path).invalidNel()
      }
    }
  }
}
