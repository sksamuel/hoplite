package com.sksamuel.hoplite.decoder

import arrow.core.toPair
import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class PairDecoder : NonNullableDecoder<Pair<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Pair::class

  override fun safeDecode(value: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Pair<*, *>> {

    fun decode(node: ListValue): ConfigResult<Pair<Any?, Any?>> {
      return if (node.elements.size == 2) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val adecoder = registry.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, registry) }
        val bdecoder = registry.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, registry) }
        arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(),
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel()) { it.toPair() }
          .leftMap { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Pair requires a list of two elements but list had size ${node.elements.size}").invalid()
    }

    return when (value) {
      is ListValue -> decode(value)
      else -> ConfigFailure.DecodeError(value, type).invalid()
    }
  }
}

class TripleDecoder : NonNullableDecoder<Triple<*, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Triple::class

  override fun safeDecode(value: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Triple<*, *, *>> {

    fun decode(a: Value, b: Value, c: Value): ConfigResult<Triple<Any?, Any?, Any?>> {
      val aType = type.arguments[0].type!!
      val bType = type.arguments[1].type!!
      val cType = type.arguments[2].type!!
      val adecoder = registry.decoder(aType).flatMap { it.decode(a, aType, registry) }
      val bdecoder = registry.decoder(bType).flatMap { it.decode(b, bType, registry) }
      val cdecoder = registry.decoder(cType).flatMap { it.decode(c, cType, registry) }
      return arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(),
        adecoder.toValidatedNel(),
        bdecoder.toValidatedNel(),
        cdecoder.toValidatedNel()) { Triple(it.a, it.b, it.c) }
        .leftMap { ConfigFailure.TupleErrors(value, it) }
    }

    fun decode(node: StringValue): ConfigResult<Triple<Any?, Any?, Any?>> {
      val parts = node.value.split(',')
      return if (parts.size == 3) {
        decode(node.copy(value = parts[0]), node.copy(value = parts[1]), node.copy(value = parts[2]))
      } else ConfigFailure.Generic("Triple requires a list of three elements but list had size ${parts.size}").invalid()
    }

    return when (value) {
      is ListValue -> decode(value.atIndex(0), value.atIndex(1), value.atIndex(2))
      is StringValue -> decode(value)
      else -> ConfigFailure.DecodeError(value, type).invalid()
    }
  }
}
