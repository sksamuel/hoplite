package com.sksamuel.hoplite.converter

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConversionFailure
import com.sksamuel.hoplite.Cursor
import kotlin.reflect.KClass

class EnumConverterProvider : ConverterProvider {
  override fun <T : Any> provide(targetType: KClass<T>): Converter<T>? {
    @Suppress("UNCHECKED_CAST")
    return if (targetType.java.isEnum) EnumConverter(targetType) else null
  }
}

class EnumConverter<T : Any>(private val klass: KClass<T>) : Converter<T> {
  override fun apply(cursor: Cursor): ConfigResult<T> {
    val t = klass.java.enumConstants.find { it.toString() == cursor.value() }
    return t?.validNel() ?: ConversionFailure(klass, cursor.value()).invalidNel()
  }
}