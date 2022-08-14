@file:Suppress("UNCHECKED_CAST")

package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import java.util.SortedSet
import java.util.TreeSet
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class SortedSetDecoder : NullHandlingDecoder<SortedSet<*>> {

  override fun supports(type: KType): Boolean {
    return type.isSubtypeOf(SortedSet::class.starProjectedType) &&
      (type.arguments[0].type?.isSubtypeOf(Comparable::class.starProjectedType) ?: false)
  }

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<SortedSet<*>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun decode(node: ArrayNode, decoder: Decoder<*>): ConfigResult<SortedSet<*>> {
      return node.elements.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it as List<Comparable<*>> }
        .map { it.toCollection(TreeSet()) }
    }

    fun decode(node: StringNode, decoder: Decoder<*>): ConfigResult<SortedSet<*>> {
      val tokens = node.value.split(",").map {
        StringNode(it.trim(), node.pos, node.path, emptyMap())
      }
      return tokens.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it as List<Comparable<*>> }
        .map { it.toCollection(TreeSet()) }
    }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is ArrayNode -> decode(node, decoder)
        is StringNode -> decode(node, decoder)
        else -> ConfigFailure.UnsupportedCollectionType(node, "SortedSet").invalid()
      }
    }
  }
}
