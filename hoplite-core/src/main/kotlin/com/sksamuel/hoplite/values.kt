package com.sksamuel.hoplite

sealed class Value {
  abstract val pos: Pos
}

sealed class Pos {
  object NoPos : Pos()
  data class LinePos(val line: Long) : Pos()
  data class LineColPos(val line: Int, val col: Int) : Pos()
  data class OffsetPos(val offset: Long) : Pos()
  data class RangePos(val start: Long, val end: Long) : Pos()
}

// -- basic types

data class StringValue(val value: String, override val pos: Pos) : Value()
data class LongValue(val value: Long, override val pos: Pos) : Value()
data class IntValue(val value: Int, override val pos: Pos) : Value()
data class BooleanValue(val value: Boolean, override val pos: Pos) : Value()
data class DoubleValue(val value: Double, override val pos: Pos) : Value()
data class FloatValue(val value: Float, override val pos: Pos) : Value()
data class NullValue(override val pos: Pos) : Value()

// -- container types

data class MapValue(val value: Map<String, Value>, override val pos: Pos) : Value()
data class ListValue(val values: List<Value>, override val pos: Pos) : Value()
