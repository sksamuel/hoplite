package com.sksamuel.hoplite.decoder

import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class Tuple2Decoder : NonNullableDecoder<Tuple2<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple2::class

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Tuple2<*, *>> {

    fun decode(node: ListNode): ConfigResult<Tuple2<Any?, Any?>> {
      return if (node.elements.size == 2) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val adecoder = registry.decoder(aType, path).flatMap { it.decode(node.atIndex(0), aType, registry, path) }
        val bdecoder = registry.decoder(bType, path).flatMap { it.decode(node.atIndex(1), bType, registry, path) }
        arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(), adecoder, bdecoder) { it }
      } else ConfigFailure.Generic("Tuple2 requires a list of two elements but list had size ${node.elements.size}").invalidNel()
    }

    return when (node) {
      is ListNode -> decode(node)
      else -> ConfigFailure.TypeConversionFailure(node, path, type).invalidNel()
    }
  }
}

class Tuple3Decoder : NonNullableDecoder<Tuple3<*, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple3::class

  override fun safeDecode(node: Node,
                          type: KType,
                          registry: DecoderRegistry,
                          path: String): ConfigResult<Tuple3<*, *, *>> {

    fun decode(node: ListNode): ConfigResult<Tuple3<Any?, Any?, Any?>> {
      return if (node.elements.size == 3) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val cType = type.arguments[2].type!!
        val adecoder = registry.decoder(aType, path).flatMap { it.decode(node.atIndex(0), aType, registry, path) }
        val bdecoder = registry.decoder(bType, path).flatMap { it.decode(node.atIndex(1), bType, registry, path) }
        val cdecoder = registry.decoder(cType, path).flatMap { it.decode(node.atIndex(2), cType, registry, path) }
        arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(), adecoder, bdecoder, cdecoder) { it }
      } else ConfigFailure.Generic("Tuple3 requires a list of three elements but list had size ${node.elements.size}").invalidNel()
    }

    return when (node) {
      is ListNode -> decode(node)
      else -> ConfigFailure.TypeConversionFailure(node, path, type).invalidNel()
    }
  }
}
