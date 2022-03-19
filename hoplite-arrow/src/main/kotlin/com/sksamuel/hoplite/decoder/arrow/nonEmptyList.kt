package com.sksamuel.hoplite.decoder.arrow

import arrow.core.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class NonEmptyListDecoder : NullHandlingDecoder<NonEmptyList<*>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(NonEmptyList::class.starProjectedType) ||
      type.isSubtypeOf(NonEmptyList::class.starProjectedType.withNullability(true))

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<NonEmptyList<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(node: StringNode, decoder: Decoder<T>): ConfigResult<NonEmptyList<T>> {
      return node.value.split(",").map { it.trim() }
        .map { decoder.decode(StringNode(it, node.pos, node.path), type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { NonEmptyList.fromListUnsafe(it) }
    }

    fun <T> decode(node: ArrayNode, decoder: Decoder<T>): ConfigResult<NonEmptyList<T>> {
      return node.elements.map { decoder.decode(it, type, context) }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .flatMap { ts ->
          NonEmptyList.fromList(ts).fold(
            { ConfigFailure.DecodeError(node, type).invalid() },
            { it.valid() }
          )
        }
    }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is ArrayNode -> decode(node, decoder)
        is StringNode -> decode(node, decoder)
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
