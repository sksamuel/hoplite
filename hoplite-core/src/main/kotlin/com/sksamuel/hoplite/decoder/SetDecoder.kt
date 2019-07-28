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

class SetDecoder : NonNullableDecoder<Set<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(Set::class.starProjectedType)

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Set<*>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun <T> decode(node: ListNode, decoder: Decoder<T>): ConfigResult<Set<T>> {
      return node.elements.map { decoder.decode(it, type, registry, path) }.sequence().map { it.toSet() }
    }

    fun <T> decode(node: StringNode, decoder: Decoder<T>): ConfigResult<Set<T>> {
      val tokens = node.value.split(",").map { it.trim() }
      return tokens.map {
        decoder.decode(StringNode(it, node.pos, node.dotpath), type, registry, path)
      }.sequence().map { it.toSet() }
    }

    return registry.decoder(t, path).flatMap { decoder ->
      when (node) {
        is ListNode -> decode(node, decoder)
        is StringNode -> decode(node, decoder)
        else -> ConfigFailure("Unsupported list type ${node.javaClass.name}").invalidNel()
      }
    }
  }
}
