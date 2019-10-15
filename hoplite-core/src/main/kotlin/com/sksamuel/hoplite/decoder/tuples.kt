package com.sksamuel.hoplite.decoder

import arrow.core.NonEmptyList
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.Tuple4
import arrow.core.Validated
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.fix
import arrow.core.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import kotlin.reflect.KType

class Tuple2Decoder : NonNullableDecoder<Tuple2<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple2::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Tuple2<*, *>> {

    fun decode(node: ArrayNode): ConfigResult<Tuple2<Any?, Any?>> {
      return if (node.elements.size == 2) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val adecoder = context.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, context) }
        val bdecoder = context.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, context) }
        Validated.applicative(NonEmptyList.semigroup<ConfigFailure>()).map(
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel()) { it }
          .fix()
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

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Tuple3<*, *, *>> {

    fun decode(node: ArrayNode): ConfigResult<Tuple3<Any?, Any?, Any?>> {
      return if (node.elements.size == 3) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val cType = type.arguments[2].type!!
        val adecoder = context.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, context) }
        val bdecoder = context.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, context) }
        val cdecoder = context.decoder(cType).flatMap { it.decode(node.atIndex(2), cType, context) }
        Validated.applicative(NonEmptyList.semigroup<ConfigFailure>()).map(
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel(),
          cdecoder.toValidatedNel()) { it }
          .fix()
          .leftMap { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Tuple3 requires a list of three elements but list had size ${node.elements.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}

class Tuple4Decoder : NonNullableDecoder<Tuple4<*, *, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Tuple4::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Tuple4<*, *, *, *>> {

    fun decode(node: ArrayNode): ConfigResult<Tuple4<Any?, Any?, Any?, Any?>> {
      return if (node.elements.size == 4) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val cType = type.arguments[2].type!!
        val dType = type.arguments[3].type!!
        val adecoder = context.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, context) }
        val bdecoder = context.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, context) }
        val cdecoder = context.decoder(cType).flatMap { it.decode(node.atIndex(2), cType, context) }
        val ddecoder = context.decoder(dType).flatMap { it.decode(node.atIndex(3), cType, context) }
        Validated.applicative(
          NonEmptyList.semigroup<ConfigFailure>()).map(
          adecoder.toValidatedNel(),
          bdecoder.toValidatedNel(),
          cdecoder.toValidatedNel(),
          ddecoder.toValidatedNel()
        ) { it }
          .fix()
          .leftMap { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Tuple4 requires a list of four elements but list had size ${node.elements.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
