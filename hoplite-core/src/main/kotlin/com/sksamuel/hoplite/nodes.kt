package com.sksamuel.hoplite

import java.lang.IllegalStateException

/**
 * An ADT that models values returned from a [PropertySource].
 */
interface Value {

  /**
   * Returns the positional information of this node.
   */
  val pos: Pos

  /**
   * Returns the dot path to this [Value].
   */
  val dotpath: String

  val simplePath: String
    get() = dotpath.replace("<root>.", "")

  /**
   * Returns the [Value] stored at the given key of this node.
   *
   * Returns [UndefinedValue] if this Node is not a container type,
   * or does not have a subnode under the given key.
   */
  fun atKey(key: String): Value

  /**
   * Returns the [Value] stored at the index of this value.
   *
   * Returns [UndefinedValue] if this Node does not contain an
   * element at the given index, or is not a sequence type.
   */
  fun atIndex(index: Int): Value

  fun atPath(path: String): Value {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  val simpleName: String

  /**
   * Returns the primitive value of this node.
   */
  fun value(): Any?
}

fun Value.recover(value: Value): Value = when (this) {
  is UndefinedValue -> value
  else -> this
}

val Value.isDefined: Boolean
  get() = this !is UndefinedValue

fun Value.hasKeyAt(key: String): Boolean = atKey(key).isDefined

/**
 * Applies the given function to all string values, recursively calling into lists and maps.
 */
fun Value.transform(f: (String) -> String): Value = when (this) {
  is StringValue -> StringValue(f(value), pos, dotpath)
  is MapValue -> MapValue(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, dotpath, null)
  is ListValue -> ListValue(elements.map { it.transform(f) }, pos, dotpath)
  else -> this
}

/**
 * Applies the given function to all key names, recursively calling into lists and maps.
 */
fun Value.mapKey(f: (String) -> String): Value = when (this) {
  is MapValue -> this.copy(map = this.map.map { f(it.key) to it.value.mapKey(f) }.toMap())
  else -> this
}

fun Value.withFallback(fallback: Value): Value {
  val self = this
  return object : Value {
    override fun value(): Any? = if (self.isDefined) self.value() else fallback.value()
    override val simpleName: String = self.simpleName
    override val dotpath: String = self.dotpath
    override val pos: Pos = self.pos
    override fun atKey(key: String): Value = self.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): Value = self.atIndex(index).recover(fallback.atIndex(index))
  }
}

sealed class Pos {

  abstract val line: Int

  object NoPos : Pos() {
    override val line: Int = -1
  }

  data class FilePos(val source: String) : Pos() {
    override val line: Int = -1
  }

  data class LinePos(override val line: Int, val source: String) : Pos()
  data class LineColPos(override val line: Int, val col: Int, val source: String) : Pos()
}

fun Pos.loc() = when (this) {
  is Pos.NoPos -> ""
  is Pos.FilePos -> "($source)"
  is Pos.LineColPos -> "($source:$line:$col)"
  is Pos.LinePos -> "($source:$line)"
}

sealed class PrimitiveValue : Value {
  override fun atIndex(index: Int): Value = UndefinedValue(pos, "$dotpath$[$index]")
  override fun atKey(key: String): Value = UndefinedValue(pos, "$dotpath.$key")
}

sealed class NumberValue : PrimitiveValue()

data class StringValue(val value: String,
                       override val pos: Pos,
                       override val dotpath: String) : PrimitiveValue() {
  override val simpleName: String = "String"
  override fun value() = value
}

data class BooleanValue(val value: Boolean,
                        override val pos: Pos,
                        override val dotpath: String) : PrimitiveValue() {
  override val simpleName: String = "Boolean"
  override fun value() = value
}

data class LongValue(val value: Long, override val pos: Pos, override val dotpath: String) : NumberValue() {
  override val simpleName: String = "Long"
  override fun value() = value
}

data class DoubleValue(val value: Double, override val pos: Pos, override val dotpath: String) : NumberValue() {
  override val simpleName: String = "Double"
  override fun value() = value
}

data class NullValue(override val pos: Pos, override val dotpath: String) : PrimitiveValue() {
  override fun value(): Any? = null
  override val simpleName: String = "null"
}

data class UndefinedValue(override val pos: Pos, override val dotpath: String) : Value {
  override val simpleName: String = "undefined"
  override fun value() = throw IllegalStateException("Undefined node has no value")
  override fun atKey(key: String): Value = this
  override fun atIndex(index: Int): Value = this
}

sealed class ContainerValue : Value

data class MapValue(val map: Map<String, Value>,
                    override val pos: Pos,
                    override val dotpath: String,
                    val value: Any? = null) : ContainerValue() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): Value = get(key)
  override fun atIndex(index: Int): Value = UndefinedValue(pos, "$dotpath$[$index]")
  operator fun get(key: String): Value = map.getOrDefault(key, UndefinedValue(pos, "$dotpath.$key"))
  override fun value(): Any? = value
}

data class ListValue(val elements: List<Value>,
                     override val pos: Pos,
                     override val dotpath: String) : ContainerValue() {
  override val simpleName: String = "List"
  override fun value(): Any? = null
  override fun atKey(key: String): Value = UndefinedValue(pos, "$dotpath.$key")
  override fun atIndex(index: Int): Value = elements.getOrElse(index) { UndefinedValue(pos, "$dotpath$[$index]") }
  operator fun get(index: Int): Value = atIndex(index)
}
