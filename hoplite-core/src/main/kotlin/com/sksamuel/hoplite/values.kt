package com.sksamuel.hoplite

sealed class Value {
  abstract val pos: Pos
}

sealed class Pos {

  abstract val line: Int

  object NoPos : Pos() {
    override val line: Int = -1
  }

  data class LinePos(override val line: Int) : Pos()
  data class LineColPos(override val line: Int, val col: Int) : Pos()
  data class RangePos(val start: Int, val end: Int) : Pos() {
    override val line: Int = start
  }
}

sealed class PrimitiveValue : Value()
sealed class NumberValue : PrimitiveValue()

data class StringValue(val value: String, override val pos: Pos) : PrimitiveValue()
data class BooleanValue(val value: Boolean, override val pos: Pos) : PrimitiveValue()
data class LongValue(val value: Long, override val pos: Pos) : NumberValue()
data class DoubleValue(val value: Double, override val pos: Pos) : NumberValue()
data class NullValue(override val pos: Pos) : PrimitiveValue()

sealed class ContainerValue : Value()

data class MapValue(val map: Map<String, Value>, override val pos: Pos) : ContainerValue() {
  operator fun get(key: String): Value = map.getOrDefault(key, NullValue(pos))
}

data class ListValue(val values: List<Value>, override val pos: Pos) : ContainerValue()
