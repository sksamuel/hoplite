package com.sksamuel.hoplite

import java.lang.IllegalStateException

/**
 * An ADT that models values returned from a [PropertySource].
 */
interface TreeNode {

  /**
   * Returns the positional information of this value.
   */
  val pos: Pos

  /**
   * If this is a [MapNode] returns the [TreeNode] stored at the given key.
   * Otherwise returns [Undefined] if this is not a [MapNode], or the
   * map does not contain a value at the given key.
   */
  fun atKey(key: String): TreeNode

  /**
   * Returns the [TreeNode] stored at the given index of this node.
   *
   * Returns [Undefined] if this value does not contain an
   * element at the given index, or is not an [ArrayNode].
   */
  fun atIndex(index: Int): TreeNode

  fun atPath(path: String): TreeNode {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  val simpleName: String

  /**
   * Returns the value stored at this node. For [PrimitiveNode] instances that is
   * the instance itself, for [MapNode]s it is the value stored at the map level.
   */
  fun value(): Any?

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
  is StringNode -> StringNode(f(value), pos)
  is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, null)
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
    override fun value(): Any? = if (self.isDefined) self.value() else fallback.value()
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

sealed class PrimitiveNode : TreeNode {
  override fun atIndex(index: Int): TreeNode = Undefined
  override fun atKey(key: String): TreeNode = Undefined
  override val size: Int = 0
}

sealed class NumberNode : PrimitiveNode()

data class StringNode(val value: String,
                      override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "String"
  override fun value() = value
}

data class BooleanNode(val value: Boolean,
                       override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "Boolean"
  override fun value() = value
}

data class LongNode(val value: Long, override val pos: Pos) : NumberNode() {
  override val simpleName: String = "Long"
  override fun value() = value
}

data class DoubleNode(val value: Double, override val pos: Pos) : NumberNode() {
  override val simpleName: String = "Double"
  override fun value() = value
}

data class NullNode(override val pos: Pos) : PrimitiveNode() {
  override fun value(): Any? = null
  override val simpleName: String = "null"
}

object Undefined : TreeNode {
  override val simpleName: String = "Undefined"
  override val pos: Pos = Pos.NoPos
  override fun value() = throw IllegalStateException("Undefined has no value")
  override fun atKey(key: String): TreeNode = this
  override fun atIndex(index: Int): TreeNode = this
  override val size: Int = 0
}

sealed class ContainerNode : TreeNode

data class MapNode(val map: Map<String, TreeNode>,
                   override val pos: Pos,
                   val value: Any? = null) : ContainerNode() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): TreeNode = get(key)
  override fun atIndex(index: Int): TreeNode = Undefined
  operator fun get(key: String): TreeNode = map.getOrDefault(key, Undefined)
  override fun value(): Any? = value
  override val size: Int = map.size
}

data class ArrayNode(val elements: List<TreeNode>,
                     override val pos: Pos) : ContainerNode() {
  override val simpleName: String = "List"
  override fun value(): Any? = null
  override fun atKey(key: String): TreeNode = Undefined
  override fun atIndex(index: Int): TreeNode = elements.getOrElse(index) { Undefined }
  operator fun get(index: Int): TreeNode = atIndex(index)
  override val size: Int = elements.size
}
