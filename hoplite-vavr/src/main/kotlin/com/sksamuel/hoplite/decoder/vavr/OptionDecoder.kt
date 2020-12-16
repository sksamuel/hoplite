package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kotlin.reflect.KType

class OptionDecoder : Decoder<Option<*>> {

  override fun supports(type: KType): Boolean = type.classifier == Option::class

  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Option<*>> {
    require(type.arguments.size == 1)

    val t = type.arguments[0].type!!

    fun <T> decode(value: Node, decoder: Decoder<T>): ConfigResult<Option<T>> =
      decoder.decode(value, t, context).map { some(it) }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is Undefined -> none<Option<*>>().valid()
        is NullNode -> none<Option<*>>().valid()
        is StringNode, is LongNode, is DoubleNode, is BooleanNode -> decode(node, decoder)
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
