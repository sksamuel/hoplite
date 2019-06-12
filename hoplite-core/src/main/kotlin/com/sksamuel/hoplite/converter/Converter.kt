package com.sksamuel.hoplite.converter

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface Converter<T> {
  fun apply(cursor: Cursor): ConfigResult<T>
}

interface ConverterProvider {
  fun <T : Any> provide(type: KType): Converter<T>?
}

abstract class ParameterizedConverterProvider<B> : ConverterProvider {
  abstract fun converter(): Converter<B>
  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> provide(type: KType): Converter<T>? {
    val ptype = this.javaClass.genericSuperclass as ParameterizedType
    val typeArg = ptype.actualTypeArguments[0].typeName
    return when (val c = type.classifier) {
      is KClass<*> -> if (typeArg == c.javaObjectType.name) converter() as Converter<T> else null
      else -> null
    }
  }
}

fun converterFor(type: KType): ConfigResult<Converter<*>> {
  return when (val c = type.classifier) {
    is KClass<*> -> if (c.isData) DataClassConverter(c).validNel() else locateConverter<Any>(type)
    else -> ConfigFailure("Unsupported classifer $type").invalidNel()
  }
}

fun <T : Any> locateConverter(type: KType): ConfigResult<Converter<T>> {
  val readers = ServiceLoader.load(ConverterProvider::class.java).toList()
  return readers.mapNotNull { it.provide<T>(type) }.firstOrNull().toOption().fold(
      { ConfigFailure.unsupportedType(type).invalidNel() },
      { it.validNel() }
  )
}