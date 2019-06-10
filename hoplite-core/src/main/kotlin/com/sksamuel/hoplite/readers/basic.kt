package com.sksamuel.hoplite.readers

import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import kotlin.reflect.KClass

class StringReader : Reader<String> {
  override fun read(cursor: ConfigCursor): ConfigResult<String> = when (val v = cursor.value()) {
    is String -> v.validNel()
    else -> ConfigFailure.conversionFailure<String>(v).invalidNel()
  }
}

class DoubleReader : Reader<Double> {
  override fun read(cursor: ConfigCursor): ConfigResult<Double> = when (val v = cursor.value()) {
    is Double -> v.validNel()
    else -> ConfigFailure.conversionFailure<Double>(v).invalidNel()
  }
  override fun supports(c: KClass<*>): Boolean = c == Double::class
}

class FloatReader : Reader<Float> {
  override fun read(cursor: ConfigCursor): ConfigResult<Float> = when (val v = cursor.value()) {
    is Float -> v.validNel()
    is Double -> v.toFloat().validNel()
    else -> ConfigFailure.conversionFailure<Float>(v).invalidNel()
  }
  override fun supports(c: KClass<*>): Boolean = c == Float::class
}

class IntReader : Reader<Int> {
  override fun read(cursor: ConfigCursor): ConfigResult<Int> = when (val v = cursor.value()) {
    is Int -> v.validNel()
    else -> ConfigFailure.conversionFailure<Int>(v).invalidNel()
  }
  override fun supports(c: KClass<*>): Boolean = c == Int::class
}

class LongReader : Reader<Long> {
  override fun read(cursor: ConfigCursor): ConfigResult<Long> = when (val v = cursor.value()) {
    is Long -> v.validNel()
    is Int -> v.toLong().validNel()
    else -> ConfigFailure.conversionFailure<Long>(v).invalidNel()
  }
  override fun supports(c: KClass<*>): Boolean = c == Long::class
}

class BooleanReader : Reader<Boolean> {
  override fun read(cursor: ConfigCursor): ConfigResult<Boolean> = when (val v = cursor.value()) {
    is Boolean -> v.validNel()
    else -> ConfigFailure.conversionFailure<Boolean>(v).invalidNel()
  }
  override fun supports(c: KClass<*>): Boolean = c == Boolean::class
}