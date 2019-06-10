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
  fun <T : Any> provide(targetType: KClass<T>): Converter<T>?
}

abstract class ParameterizedConverterProvider<B> : ConverterProvider {
  abstract fun converter(): Converter<B>
  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> provide(targetType: KClass<T>): Converter<T>? {
    val ptype = this.javaClass.genericSuperclass as ParameterizedType
    val typeArg = ptype.actualTypeArguments[0].typeName
    return if (typeArg == targetType.javaObjectType.name) converter() as Converter<T> else null
  }
}

fun locateConverter(type: KType): ConfigResult<Converter<*>> {
  val readers = ServiceLoader.load(ConverterProvider::class.java).toList()
  return when (val c = type.classifier) {
    is KClass<*> -> {
      readers.mapNotNull { it.provide(c) }.firstOrNull().toOption().fold(
          { ConfigFailure.unsupportedType(type).invalidNel() },
          { it.validNel() }
      )
    }
    else -> ConfigFailure("Unsupported classifer $type").invalidNel()
  }
}