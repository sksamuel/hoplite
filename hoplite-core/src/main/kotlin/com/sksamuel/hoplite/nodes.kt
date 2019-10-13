package com.sksamuel.hoplite

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.lang.IllegalStateException

interface TreeNode {

  /**
   * Returns the positional information of this value.
   */
  val pos: Pos

  /**
   * Returns the [PrimitiveNode] at the given key.
   * If this node is not a [MapNode] or the node contained at the
   * given key is not a primitive, or the node does not contain
   * the key at all, then this will return [Undefined].
   */
  fun atKey(key: String): TreeNode

  /**
   * Returns the [MapNode] at the given key.
   * If this node is not a [MapNode] or the node contained at the
   * given key is not a map, or the node does not contain the
   * key at all, then this will returned [Undefined]
   */
  fun subtree(key: String): Either<Undefined, MapNode> = Undefined.left()

  /**
   * Returns the [TreeNode] stored at the given index of this node.
   *
   * Returns [Undefined] if this node does not contain an
   * element at the given index, or is not an [ArrayNode].
   */
  fun atIndex(index: Int): TreeNode

  @Deprecated("tbm")
  fun atPath(path: String): TreeNode {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  val simpleName: String

  /**
   * Returns the [Value] associated with this node.
   * For a [PrimitiveNode] that is the underlying value, for a [MapNode]
   * it is the value stored at the map level, and for [ArrayNode] it
   * is [Value.NullValue].
   */
  val value: Value

  /**
   * Returns he number of child nodes this node contains. That is, for [ArrayNode] it is
   * the number of child nodes; for [MapNode] it is the number of fields, and for all
   * other nodes it is 0.
   */
  val size: Int
}

fun TreeNode.recover(value: TreeNode): TreeNode = when (this) {
  is Undefined -> value
  else -> this
}

val TreeNode.isDefined: Boolean
  get() = this !is Undefined

fun TreeNode.hasKeyAt(key: String): Boolean = atKey(key).isDefined

/**
 * Applies the given function to all string values, recursively calling into lists and maps.
 */
fun TreeNode.transform(f: (String) -> String): TreeNode = when (this) {
  is PrimitiveNode -> when (val primitive = this.value) {
    is Value.StringNode -> this.copy(value = Value.StringNode(f(primitive.value)))
    else -> this
  }
  is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, this.value)
  is ArrayNode -> ArrayNode(elements.map { it.transform(f) }, pos)
  else -> this
}

/**
 * Applies the given function to all key names, recursively calling into lists and maps.
 */
fun TreeNode.mapKey(f: (String) -> String): TreeNode = when (this) {
  is MapNode -> this.copy(map = this.map.map { f(it.key) to it.value.mapKey(f) }.toMap())
  else -> this
}

fun TreeNode.withFallback(fallback: TreeNode): TreeNode {
  val self = this
  return object : TreeNode {
    override val size: Int = 0
    override val value: Value = TODO()
    override val simpleName: String = self.simpleName
    override val pos: Pos = self.pos
    override fun atKey(key: String): TreeNode = self.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): TreeNode = self.atIndex(index).recover(fallback.atIndex(index))
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

sealed class ContainerNode : TreeNode

data class MapNode(val map: Map<String, TreeNode>,
                   override val pos: Pos,
                   override val value: Value = Value.NullValue) : ContainerNode() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): TreeNode = get(key)
  override fun atIndex(index: Int): TreeNode = Undefined
  operator fun get(key: String): TreeNode = map.getOrDefault(key, Undefined)
  override val size: Int = map.size

  override fun subtree(key: String): Either<Undefined, MapNode> {
    return when (val node = map[key]) {
      is MapNode -> node.right()
      else -> Undefined.left()
    }
  }
}

data class ArrayNode(val elements: List<TreeNode>,
                     override val pos: Pos) : ContainerNode() {
  override val simpleName: String = "List"
  override val value: Value = Value.NullValue
  override fun atKey(key: String): TreeNode = Undefined
  override fun atIndex(index: Int): TreeNode = elements.getOrElse(index) { Undefined }
  operator fun get(index: Int): TreeNode = atIndex(index)
  override val size: Int = elements.size
}

data class PrimitiveNode(override val value: Value, override val pos: Pos) : TreeNode {
  override val simpleName: String = value.simpleName
  override fun atKey(key: String): TreeNode = Undefined
  override fun atIndex(index: Int): TreeNode = Undefined
  override val size: Int = 0
}

sealed class Value {

  abstract class NumberValue : Value()

  abstract val simpleName: String

  data class StringNode(val value: String) : Value() {
    override val simpleName: String = "String"
  }

  data class BooleanNode(val value: Boolean) : Value() {
    override val simpleName: String = "Boolean"
  }

  data class LongNode(val value: Long) : NumberValue() {
    override val simpleName: String = "Long"
  }

  data class DoubleNode(val value: Double) : NumberValue() {
    override val simpleName: String = "Double"
  }

  object NullValue : Value() {
    override val simpleName: String = "null"
  }
}

object Undefined : TreeNode {
  override val simpleName: String = "Undefined"
  override val pos: Pos = Pos.NoPos
  override val value = throw IllegalStateException("Undefined has no value")
  override fun atKey(key: String): TreeNode = this
  override fun atIndex(index: Int): TreeNode = this
  override val size: Int = 0
}
