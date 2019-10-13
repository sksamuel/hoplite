package com.sksamuel.hoplite

import arrow.core.Either
import arrow.core.left
import arrow.core.right

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
  is StringNode -> this.copy(value = f(this.value))
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
                   val value: TreeNode = Undefined) : ContainerNode() {
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
  override fun atKey(key: String): TreeNode = Undefined
  override fun atIndex(index: Int): TreeNode = elements.getOrElse(index) { Undefined }
  operator fun get(index: Int): TreeNode = atIndex(index)
  override val size: Int = elements.size
}

sealed class PrimitiveNode : TreeNode {
  override fun atKey(key: String): TreeNode = Undefined
  override fun atIndex(index: Int): TreeNode = Undefined
  override val size: Int = 0
  abstract val value: Any?
}

data class StringNode(override val value: String, override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "String"
}

data class BooleanNode(override val value: Boolean, override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "Boolean"
}

sealed class NumberNode : PrimitiveNode()

data class LongNode(override val value: Long, override val pos: Pos) : NumberNode() {
  override val simpleName: String = "Long"
}

data class DoubleNode(override val value: Double, override val pos: Pos) : NumberNode() {
  override val simpleName: String = "Double"
}

data class NullValue(override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "null"
  override val value: Any? = null
}

object Undefined : TreeNode {
  override val simpleName: String = "Undefined"
  override val pos: Pos = Pos.NoPos
  override fun atKey(key: String): TreeNode = this
  override fun atIndex(index: Int): TreeNode = this
  override val size: Int = 0
}
