package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConversionFailure
import com.sksamuel.hoplite.Node
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
class EnumDecoder<T : Any> : Decoder<T> {

  override fun supports(type: KType): Boolean = type.classifier is KClass<*> && (type.classifier as KClass<*>).java.isEnum

  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<T> {
    val klass = type.classifier as KClass<*>
    val t = klass.java.enumConstants.find { it.toString().validNel() == node.string() } as T?
    return t?.validNel() ?: ConversionFailure(klass, node).invalidNel()
  }

}