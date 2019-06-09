package com.sksamuel.hoplite.readers

import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigResult
import kotlin.reflect.KClass

class StringReader : Reader<String> {
  override fun read(cursor: ConfigCursor): ConfigResult<String> = cursor.asString()
}

class DoubleReader : Reader<Double> {
  override fun read(cursor: ConfigCursor): ConfigResult<Double> = cursor.asDouble()
  override fun supports(c: KClass<*>): Boolean = c == Double::class
}

class FloatReader : Reader<Float> {
  override fun read(cursor: ConfigCursor): ConfigResult<Float> = cursor.asFloat()
  override fun supports(c: KClass<*>): Boolean = c == Float::class
}

class IntReader : Reader<Int> {
  override fun read(cursor: ConfigCursor): ConfigResult<Int> = cursor.asInt()
  override fun supports(c: KClass<*>): Boolean = c == Int::class
}

class LongReader : Reader<Long> {
  override fun read(cursor: ConfigCursor): ConfigResult<Long> = cursor.asLong()
  override fun supports(c: KClass<*>): Boolean = c == Long::class
}

class BooleanReader : Reader<Boolean> {
  override fun read(cursor: ConfigCursor): ConfigResult<Boolean> = cursor.asBoolean()
  override fun supports(c: KClass<*>): Boolean = c == Boolean::class
}