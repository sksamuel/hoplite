package com.sksamuel.hoplite.decoder

import arrow.core.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class SortedSetDecoder<T : Comparable<T>> : NonNullableDecoder<SortedSet<T>> {

  override fun supports(type: KType): Boolean {
    return type.isSubtypeOf(Set::class.starProjectedType) &&
      (type.arguments[0].type?.isSubtypeOf(Comparable::class.starProjectedType) ?: false)
  }

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<SortedSet<T>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<SortedSet<T>> {
      return node.elements.map { decoder.decode(it, type, context) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toSortedSet() }
    }

    fun decode(node: StringNode, decoder: Decoder<T>): ConfigResult<SortedSet<T>> {
      val tokens = node.value.split(",").map {
        StringNode(it.trim(), node.pos)
      }
      return tokens.map { decoder.decode(it, type, context) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toSortedSet() }
    }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is ArrayNode -> decode(node, decoder as Decoder<T>)
        is StringNode -> decode(node, decoder as Decoder<T>)
        else -> ConfigFailure.UnsupportedCollectionType(node, "SortedSet").invalid()
      }
    }
  }
}
