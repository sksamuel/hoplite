package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import kotlin.reflect.KType
import kotlin.reflect.full.createType

inline fun <T, reified U> Decoder<T>.map(crossinline f: (T) -> U): Decoder<U> = object : Decoder<U> {
  override fun supports(type: KType): Boolean = U::class.createType() == type
  override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<U> {
    return this@map.decode(node, type, context).map { f(it) }
  }
}

