@file:Suppress("UNCHECKED_CAST")

package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import io.vavr.collection.SortedSet
import io.vavr.collection.TreeSet
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class SortedSetDecoder<T : Comparable<T>> : NullHandlingDecoder<SortedSet<T>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(SortedSet::class.starProjectedType) &&
      (type.arguments[0].type?.isSubtypeOf(Comparable::class.starProjectedType) ?: false)

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<SortedSet<T>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<SortedSet<T>> =
      node.elements.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { TreeSet.ofAll(it) }

    fun decode(node: StringNode, decoder: Decoder<T>): ConfigResult<SortedSet<T>> {
      val tokens = node.value.split(",").map {
        StringNode(it.trim(), node.pos)
      }

      return tokens.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { TreeSet.ofAll(it) }
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
