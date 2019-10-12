package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class ListDecoder : Decoder<List<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(List::class.starProjectedType)

  override fun decode(value: TreeNode,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<List<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<List<T>> {
      return node.elements.map { decoder.decode(it, t, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
    }

    fun <T> decode(node: StringNode, decoder: Decoder<T>): ConfigResult<List<T>> {
      val tokens = node.value.split(",").map { StringNode(it.trim(), node.pos) }
      return tokens.map { decoder.decode(it, t, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (value) {
        is ArrayNode -> decode(value, decoder)
        is StringNode -> decode(value, decoder)
        else -> ConfigFailure.UnsupportedCollectionType(value, "List").invalid()
      }
    }
  }
}
