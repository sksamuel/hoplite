package com.sksamuel.hoplite

import arrow.data.validNel

/**
 * An ADT that models the tree returned from config files.
 */
interface Value {

  /**
   * Returns the positional information of this value.
   */
  val pos: Pos

  /**
   * Returns the [Value] stored at the given key of this value.
   */
  fun atKey(key: String): Value

  /**
   * Returns the [Value] stored at the index of this value
   */
  fun atIndex(index: Int): Value

  fun atPath(path: String): Value {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  /**
   * Returns the underlying [Value] as a String, if the value is
   * a [StringValue] type, otherwise returns an error.
   */
  fun string(): ConfigResult<String> = when (this) {
    is StringValue -> this.value.validNel()
    else -> ConfigResults.failedTypeConversion(this)
  }

  fun transform(f: (String) -> String): Value = when (this) {
    is StringValue -> StringValue(f(value), pos)
    is MapValue -> MapValue(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos)
    is ListValue -> ListValue(values.map { it.transform(f) }, pos)
    else -> this
  }

  fun withFallback(fallback: Value): Value = object : Value {
    override val pos: Pos = this@Value.pos
    override fun atKey(key: String): Value = this@Value.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): Value = this@Value.atIndex(index).recover(fallback.atIndex(index))
  }

  fun recover(value: Value): Value = when (this) {
    is UndefinedValue -> value
    else -> this
  }
}

sealed class Pos {

  abstract val line: Int

  object NoPos : Pos() {
    override val line: Int = -1
  }

  data class LinePos(override val line: Int) : Pos()
  data class LineColPos(override val line: Int, val col: Int) : Pos()
}

sealed class PrimitiveValue : Value {
  abstract val value: Any?
  override fun atIndex(index: Int): Value = UndefinedValue(pos)
  override fun atKey(key: String): Value = UndefinedValue(pos)
}

sealed class NumberValue : PrimitiveValue()

data class StringValue(override val value: String, override val pos: Pos) : PrimitiveValue()
data class BooleanValue(override val value: Boolean, override val pos: Pos) : PrimitiveValue()
data class LongValue(override val value: Long, override val pos: Pos) : NumberValue()
data class DoubleValue(override val value: Double, override val pos: Pos) : NumberValue()
data class NullValue(override val pos: Pos) : PrimitiveValue() {
  override val value: Any? = null
}

data class UndefinedValue(override val pos: Pos) : Value {
  override fun atKey(key: String): Value = this
  override fun atIndex(index: Int): Value = this
}

sealed class ContainerValue : Value

data class MapValue(val map: Map<String, Value>, override val pos: Pos) : ContainerValue() {
  override fun atKey(key: String): Value = get(key)
  override fun atIndex(index: Int): Value = UndefinedValue(pos)
  operator fun get(key: String): Value = map.getOrDefault(key, UndefinedValue(pos))
}

data class ListValue(val values: List<Value>, override val pos: Pos) : ContainerValue() {
  override fun atKey(key: String): Value = UndefinedValue(pos)
  override fun atIndex(index: Int): Value = values.getOrElse(index) { UndefinedValue(pos) }
  operator fun get(index: Int): Value = atIndex(index)

}
