package com.sksamuel.hoplite.decoder

import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class SetDecoder : NonNullableDecoder<Set<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(Set::class.starProjectedType)

  override fun safeDecode(value: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Set<*>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun <T> decode(node: ListValue, decoder: Decoder<T>): ConfigResult<Set<T>> {
      return node.elements.map { decoder.decode(it, type, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toSet() }
    }

    fun <T> decode(node: StringValue, decoder: Decoder<T>): ConfigResult<Set<T>> {
      val tokens = node.value.split(",").map { StringValue(it.trim(), node.pos, node.dotpath) }
      return tokens.map { decoder.decode(it, type, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toSet() }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (value) {
        is ListValue -> decode(value, decoder)
        is StringValue -> decode(value, decoder)
        else -> ConfigFailure.UnsupportedCollectionType(value, "Set").invalid()
      }
    }
  }
}
