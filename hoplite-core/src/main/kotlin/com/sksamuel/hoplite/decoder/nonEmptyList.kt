package com.sksamuel.hoplite.decoder

import arrow.data.NonEmptyList
import arrow.data.invalid
import arrow.data.valid
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

class NonEmptyListDecoder : NonNullableDecoder<NonEmptyList<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(NonEmptyList::class.starProjectedType)

  override fun safeDecode(value: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<NonEmptyList<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(node: StringValue, decoder: Decoder<T>): ConfigResult<NonEmptyList<T>> {
      return node.value.split(",").map { it.trim() }
        .map { decoder.decode(StringValue(it, node.pos, node.dotpath), type, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { NonEmptyList.fromListUnsafe(it) }
    }

    fun <T> decode(node: ListValue, decoder: Decoder<T>): ConfigResult<NonEmptyList<T>> {
      return node.elements.map { decoder.decode(it, type, registry) }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .flatMap { ts ->
        NonEmptyList.fromList(ts).fold(
          { ConfigFailure.DecodeError(node, type).invalid() },
          { it.valid() }
        )
      }
    }

    return registry.decoder(t).flatMap { decoder ->
      when (value) {
        is StringValue -> decode(value, decoder)
        is ListValue -> decode(value, decoder)
        else -> ConfigFailure.DecodeError(value, type).invalid()
      }
    }
  }
}
