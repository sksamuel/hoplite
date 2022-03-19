package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import kotlin.reflect.KType

class PairDecoder : NullHandlingDecoder<Pair<*, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Pair::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Pair<*, *>> {

    fun decode(node: ArrayNode): ConfigResult<Pair<Any?, Any?>> {
      return if (node.elements.size == 2) {
        val aType = type.arguments[0].type!!
        val bType = type.arguments[1].type!!
        val adecoder = context.decoder(aType).flatMap { it.decode(node.atIndex(0), aType, context) }
        val bdecoder = context.decoder(bType).flatMap { it.decode(node.atIndex(1), bType, context) }
        Validated.ap(adecoder, bdecoder) { a, b -> Pair(a, b) }
          .mapInvalid { ConfigFailure.TupleErrors(node, it) }
      } else ConfigFailure.Generic("Pair requires a list of two elements but list had size ${node.elements.size}").invalid()
    }

    return when (node) {
      is ArrayNode -> decode(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}

class TripleDecoder : NullHandlingDecoder<Triple<*, *, *>> {

  override fun supports(type: KType): Boolean = type.classifier == Triple::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Triple<*, *, *>> {

    fun decode(a: Node, b: Node, c: Node): ConfigResult<Triple<Any?, Any?, Any?>> {
      val aType = type.arguments[0].type!!
      val bType = type.arguments[1].type!!
      val cType = type.arguments[2].type!!
      val adecoder = context.decoder(aType).flatMap { it.decode(a, aType, context) }
      val bdecoder = context.decoder(bType).flatMap { it.decode(b, bType, context) }
      val cdecoder = context.decoder(cType).flatMap { it.decode(c, cType, context) }
      return Validated.ap(adecoder, bdecoder, cdecoder) { a, b, c -> Triple(a, b, c) }
        .mapInvalid { ConfigFailure.TupleErrors(node, it) }
    }

    fun decode(node: StringNode): ConfigResult<Triple<Any?, Any?, Any?>> {
      val parts = node.value.split(',')
      return if (parts.size == 3) {
        val a = StringNode(parts[0], node.pos, node.path)
        val b = StringNode(parts[1], node.pos, node.path)
        val c = StringNode(parts[2], node.pos, node.path)
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
