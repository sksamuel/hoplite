package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import io.vavr.collection.List
import io.vavr.kotlin.toVavrList
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class ListDecoder : NullHandlingDecoder<List<*>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(List::class.starProjectedType) ||
      type.isSubtypeOf(List::class.starProjectedType.withNullability(true))

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<List<*>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun <T> decode(node: StringNode, decoder: Decoder<T>): ConfigResult<List<T>> =
      node.value.split(",").map { it.trim() }
        .map { decoder.decode(StringNode(it, node.pos), type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toVavrList() }

    fun <T> decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<List<T>> =
      node.elements.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toVavrList() }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is ArrayNode -> decode(node, decoder)
        is StringNode -> decode(node, decoder)
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
