package com.sksamuel.hoplite.decoder

import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class Tuple2Decoder : NonNullableDecoder<Tuple2<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple2::class

  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Tuple2<*, *>> {

    fun decode(node: ArrayNode): ConfigResult<Tuple2<Any?, Any?>> {
      return if (node.elements.size == 2) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val adecoder = registry.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, registry) }
        val bdecoder = registry.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, registry) }
        arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(),
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel()) { it }
          .leftMap { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Tuple2 requires a list of two elements but list had size ${node.elements.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}

class Tuple3Decoder : NonNullableDecoder<Tuple3<*, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple3::class

  override fun safeDecode(node: TreeNode,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Tuple3<*, *, *>> {

    fun decode(node: ArrayNode): ConfigResult<Tuple3<Any?, Any?, Any?>> {
      return if (node.elements.size == 3) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val cType = type.arguments[2].type!!
        val adecoder = registry.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, registry) }
        val bdecoder = registry.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, registry) }
        val cdecoder = registry.decoder(cType).flatMap { it.decode(node.atIndex(2), cType, registry) }
        arrow.data.extensions.validated.applicative.map(NonEmptyList.semigroup(),
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel(),
          cdecoder.toValidatedNel()) { it }
          .leftMap { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Tuple3 requires a list of three elements but list had size ${node.elements.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
