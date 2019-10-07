package com.sksamuel.hoplite

import java.lang.IllegalStateException

/**
 * An ADT that models the tree returned from config files.
 */
interface Node {

  /**
   * Returns the positional information of this node.
   */
  val pos: Pos

  /**
   * Returns the dot path to this [Node].
   */
  val dotpath: String

  val simplePath: String
    get() = dotpath.replace("<root>.", "")

  /**
   * Returns the [Node] stored at the given key of this node.
   *
   * Returns [UndefinedNode] if this Node is not a container type,
   * or does not have a subnode under the given key.
   */
  fun atKey(key: String): Node

  /**
   * Returns the [Node] stored at the index of this value.
   *
   * Returns [UndefinedNode] if this Node does not contain an
   * element at the given index, or is not a sequence type.
   */
  fun atIndex(index: Int): Node

  fun atPath(path: String): Node {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  val simpleName: String

  /**
   * Returns the primitive value of this node.
   */
  fun value(): Any?
}

fun Node.recover(node: Node): Node = when (this) {
  is UndefinedNode -> node
  else -> this
}

val Node.isDefined: Boolean
  get() = this !is UndefinedNode

fun Node.hasKeyAt(key: String): Boolean = atKey(key).isDefined

/**
 * Applies the given function to all string values, recursively calling into lists and maps.
 */
fun Node.transform(f: (String) -> String): Node = when (this) {
  is StringNode -> StringNode(f(value), pos, dotpath)
  is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, dotpath, null)
  is ListNode -> ListNode(elements.map { it.transform(f) }, pos, dotpath)
  else -> this
}

/**
 * Applies the given function to all key names, recursively calling into lists and maps.
 */
fun Node.mapKey(f: (String) -> String): Node = when (this) {
  is MapNode -> this.copy(map = this.map.map { f(it.key) to it.value.mapKey(f) }.toMap())
  else -> this
}

fun Node.withFallback(fallback: Node): Node {
  val self = this
  return object : Node {
    override fun value(): Any? = if (self.isDefined) self.value() else fallback.value()
    override val simpleName: String = self.simpleName
    override val dotpath: String = self.dotpath
    override val pos: Pos = self.pos
    override fun atKey(key: String): Node = self.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): Node = self.atIndex(index).recover(fallback.atIndex(index))
  }
}

class SimpleNode(override val value: Any?,
                 override val pos: Pos,
                 private val elements: List<Node>,
                 private val children: Map<String, Node>) : Node {

  override val simpleName: String = "simplenode"
  override val dotpath: String = "dotpath"

  override fun atKey(key: String): Node = children.getOrDefault(key, UndefinedNode(pos, "$dotpath.$key"))
  override fun atIndex(index: Int): Node = elements.getOrElse(index) { UndefinedNode(pos, "$dotpath$[$index]") }
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

sealed class PrimitiveNode : Node {
  override fun atIndex(index: Int): Node = UndefinedNode(pos, "$dotpath$[$index]")
  override fun atKey(key: String): Node = UndefinedNode(pos, "$dotpath.$key")
}

sealed class NumberNode : PrimitiveNode()

data class StringNode(val value: String,
                      override val pos: Pos,
                      override val dotpath: String) : PrimitiveNode() {
  override val simpleName: String = "String"
  override fun value() = value
}

data class BooleanNode(val value: Boolean,
                       override val pos: Pos,
                       override val dotpath: String) : PrimitiveNode() {
  override val simpleName: String = "Boolean"
  override fun value() = value
}

data class LongNode(val value: Long, override val pos: Pos, override val dotpath: String) : NumberNode() {
  override val simpleName: String = "Long"
  override fun value() = value
}

data class DoubleNode(val value: Double, override val pos: Pos, override val dotpath: String) : NumberNode() {
  override val simpleName: String = "Double"
  override fun value() = value
}

data class NullNode(override val pos: Pos, override val dotpath: String) : PrimitiveNode() {
  override fun value(): Any? = null
  override val simpleName: String = "null"
}

data class UndefinedNode(override val pos: Pos, override val dotpath: String) : Node {
  override val simpleName: String = "undefined"
  override fun value() = throw IllegalStateException("Undefined node has no value")
  override fun atKey(key: String): Node = this
  override fun atIndex(index: Int): Node = this
}

sealed class ContainerNode : Node

data class MapNode(val map: Map<String, Node>,
                   override val pos: Pos,
                   override val dotpath: String,
                   val value: Any? = null) : ContainerNode() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): Node = get(key)
  override fun atIndex(index: Int): Node = UndefinedNode(pos, "$dotpath$[$index]")
  operator fun get(key: String): Node = map.getOrDefault(key, UndefinedNode(pos, "$dotpath.$key"))
  override fun value(): Any? = value
}

data class ListNode(val elements: List<Node>,
                    override val pos: Pos,
                    override val dotpath: String) : ContainerNode() {
  override val simpleName: String = "List"
  override fun value(): Any? = null
  override fun atKey(key: String): Node = UndefinedNode(pos, "$dotpath.$key")
  override fun atIndex(index: Int): Node = elements.getOrElse(index) { UndefinedNode(pos, "$dotpath$[$index]") }
  operator fun get(index: Int): Node = atIndex(index)
}
