package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class ListDecoder : Decoder<List<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(List::class.starProjectedType)

  override fun decode(node: TreeNode,
                      type: KType,
                      registry: DecoderRegistry): ConfigResult<List<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<List<T>> {
      return node.elements.map { decoder.decode(it, t, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
    }

    fun <T> decode(node: PrimitiveNode, str: String, decoder: Decoder<T>): ConfigResult<List<T>> {
      val tokens = str.split(",").map {
        PrimitiveNode(Value.StringNode(it.trim()), node.pos)
      }
      return tokens.map { decoder.decode(it, t, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (node) {
        is ArrayNode -> decode(node, decoder)
        is PrimitiveNode -> when (val v = node.value) {
          is Value.StringNode -> decode(node, v.value, decoder)
          else -> ConfigFailure.UnsupportedCollectionType(node, "List").invalid()
        }
        else -> ConfigFailure.UnsupportedCollectionType(node, "List").invalid()
      }
    }
  }
}
