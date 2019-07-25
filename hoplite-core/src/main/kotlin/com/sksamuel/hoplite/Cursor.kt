package com.sksamuel.hoplite

import arrow.data.validNel
import java.util.*

interface Cursor {

  /**
   * The file system location of the config to which this cursor points.
   */
  fun location(): ConfigLocation

  /**
   * Returns a cursor pointing to the [Value] stored at the given key of this cursor.
   */
  fun atKey(key: String): Cursor

  /**
   * Returns a cursor pointing to the [Value] stored at the index of this value
   */
  fun atIndex(index: Int): Cursor

  fun atPath(path: String): Cursor {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  /**
   * Returns the [Value] that this cursor points to.
   */
  fun value(): Value

  /**
   * Returns the underlying [Value] as a String, if the value is
   * a [StringValue] type, otherwise returns an error.
   */
  fun string(): ConfigResult<String> = when (val v = value()) {
    is StringValue -> v.value.validNel()
    else -> ConfigResults.failedTypeConversion(v)
  }

  /**
   * Returns the underlying [Value] as a String if the value can be
   * converted into a String, such as from a number type, otherwise
   * returns an error.
   */
  fun asString(): ConfigResult<String> = when (val v = value()) {
    is StringValue -> v.value.validNel()
    is DoubleValue -> v.value.toString().validNel()
    is BooleanValue -> v.value.toString().validNel()
    is LongValue -> v.value.toString().validNel()
    else -> ConfigResults.failedTypeConversion(v)
  }

  /**
   * The path in the config to which this cursor points as a list of keys, with the
   * first key in the result being the outermost part of the path.
   */
  fun path(): List<String>

  fun transform(f: (Value) -> Value): Cursor

  companion object {
    operator fun invoke(resource: String, value: Value, path: List<String>): Cursor = when (value) {
      is PrimitiveValue -> PrimitiveCursor(resource, value, path)
      is MapValue -> MapCursor(resource, value, path)
      is ListValue -> ListCursor(resource, value, path)
    }
  }
}

class MapCursor(private val resource: String,
                private val value: MapValue,
                private val path: List<String>) : Cursor {
  override fun location(): ConfigLocation = ConfigLocation(resource, value.pos)
  override fun value(): Value = value
  override fun path(): List<String> = path
  override fun atKey(key: String): Cursor = Cursor(resource, value[key], path + key)
  override fun atIndex(index: Int): Cursor = PrimitiveCursor(resource, NullValue(value.pos), path)
  override fun transform(f: (Value) -> Value): Cursor = Cursor(resource, f(value), path)
}

class ListCursor(private val resource: String,
                 private val value: ListValue,
                 private val path: List<String>) : Cursor {
  override fun location(): ConfigLocation = ConfigLocation(resource, value.pos)
  override fun value(): Value = value
  override fun path(): List<String> = path
  override fun atKey(key: String): Cursor = TODO()
  override fun atIndex(index: Int): Cursor {
    return if (index < value.values.size)
      Cursor(resource, value, path)
    else
      PrimitiveCursor(resource, NullValue(value.pos), path)
  }

  override fun transform(f: (Value) -> Value): Cursor = Cursor(resource, f(value), path)
}

class PrimitiveCursor(private val resource: String,
                      private val value: PrimitiveValue,
                      private val path: List<String>) : Cursor {
  override fun location(): ConfigLocation = ConfigLocation(resource, value.pos)
  override fun path(): List<String> = path
  override fun value(): Value = value
  override fun atKey(key: String): Cursor = PrimitiveCursor(resource, NullValue(value.pos), path + key)
  override fun atIndex(index: Int): Cursor = PrimitiveCursor(resource, NullValue(value.pos), path)
  override fun transform(f: (Value) -> Value): Cursor = Cursor(resource, f(value), path)
}