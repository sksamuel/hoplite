package com.sksamuel.hoplite.decoder

import arrow.core.toPair
import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class PairDecoder : NonNullableDecoder<Pair<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Pair::class

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Pair<*, *>> {

    fun decode(node: ArrayNode): ConfigResult<Pair<Any?, Any?>> {
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

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}

class TripleDecoder : NonNullableDecoder<Triple<*, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Triple::class

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Triple<*, *, *>> {

    fun decode(a: Node, b: Node, c: Node): ConfigResult<Triple<Any?, Any?, Any?>> {
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
        .leftMap { ConfigFailure.TupleErrors(node, it) }
    }

    fun decode(node: StringNode): ConfigResult<Triple<Any?, Any?, Any?>> {
      val parts = node.value.split(',')
      return if (parts.size == 3) {
        val a = StringNode(parts[0], node.pos)
        val b = StringNode(parts[1], node.pos)
        val c = StringNode(parts[2], node.pos)
        decode(a, b, c)
      } else ConfigFailure.Generic("Triple requires a list of three elements but list had size ${parts.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node.atIndex(0), node.atIndex(1), node.atIndex(2))
      else -> when (node) {
        is StringNode -> decode(node)
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
