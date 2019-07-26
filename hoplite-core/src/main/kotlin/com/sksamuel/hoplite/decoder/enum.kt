package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConversionFailure
import com.sksamuel.hoplite.Value
import kotlin.reflect.KClass
import kotlin.reflect.KType

class EnumDecoderFactory : DecoderFactory {
  override fun <T : Any> provide(type: KType): Decoder<T>? = when (val c = type.classifier) {
    is KClass<*> -> if (c.java.isEnum) EnumDecoder<T>(c as KClass<T>) else null
    else -> null
  }
}

class EnumDecoder<T : Any>(private val klass: KClass<T>) : Decoder<T> {
  override fun convert(value: Value, registry: DecoderRegistry): ConfigResult<T> {
    val t = klass.java.enumConstants.find { it.toString().validNel() == value.string() }
    return t?.validNel() ?: ConversionFailure(klass, value).invalidNel()
  }
}