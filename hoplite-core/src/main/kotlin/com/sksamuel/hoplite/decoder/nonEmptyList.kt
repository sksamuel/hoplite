package com.sksamuel.hoplite.decoder

import arrow.data.NonEmptyList
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigResults
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class NonEmptyListDecoder : Decoder<NonEmptyList<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(NonEmptyList::class.starProjectedType)

  private fun <T> decode(node: StringNode,
                         type: KType,
                         decoder: Decoder<T>,
                         registry: DecoderRegistry): ConfigResult<NonEmptyList<T>> {
    return node.value.split(",").map { it.trim() }
        .map { decoder.decode(StringNode(it, node.pos), type, registry) }.sequence()
        .map { NonEmptyList.fromListUnsafe(it) }
  }

  private fun <T> decode(node: ListNode,
                         type: KType,
                         decoder: Decoder<T>,
                         registry: DecoderRegistry): ConfigResult<NonEmptyList<T>> {
    return node.elements.map { decoder.decode(it, type, registry) }.sequence().flatMap { ts ->
      NonEmptyList.fromList(ts).fold(
          {
            val err = "Cannot convert empty list to NonEmptyList<${this.typeParameters[0]}>"
            ConfigResults.decodeFailure(node, err)
          },
          {
            it.validNel()
          }
      )
    }
  }

  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<NonEmptyList<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!
    return registry.decoder(t).flatMap { decoder ->
      when (node) {
        is StringNode -> decode(node, t, decoder, registry)
        is ListNode -> decode(node, t, decoder, registry)
        else -> ConfigResults.decodeFailure(node, this.typeParameters[0])
      }
    }
  }
}