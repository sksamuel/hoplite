package com.sksamuel.hoplite

import arrow.data.validNel

/**
 * An ADT that models the tree returned from config files.
 */
interface Node {

  /**
   * Returns the positional information of this value.
   */
  val pos: Pos

  /**
   * Returns the dot path to this [Node].
   */
  val dotpath: String

  val simplePath: String
    get() = dotpath.replace("<root>.", "")

  /**
   * Returns the [Node] stored at the given key of this value.
   *
   * Returns [UndefinedNode] if this Node does not define the key
   * or is not a dictionary type.
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

  fun transform(f: (String) -> String): Node = when (this) {
    is StringNode -> StringNode(f(value), pos, dotpath)
    is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, dotpath)
    is ListNode -> ListNode(elements.map { it.transform(f) }, pos, dotpath)
    else -> this
  }

  fun withFallback(fallback: Node): Node = object : Node {
    override val simpleName: String = this@Node.simpleName
    override val dotpath: String = this@Node.dotpath
    override val pos: Pos = this@Node.pos
    override fun atKey(key: String): Node = this@Node.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): Node = this@Node.atIndex(index).recover(fallback.atIndex(index))
  }

  fun recover(node: Node): Node = when (this) {
    is UndefinedNode -> node
    else -> this
  }

  val simpleName: String
}

/**
 * The file system location of a ConfigValue, represented by a url and a line
 * number
 *
 * @param url the URL describing the origin of the ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
sealed class Pos {

  abstract val line: Int

  object NoPos : Pos() {
    override val line: Int = -1
  }

  data class LineColPos(override val line: Int, val col: Int, val source: String) : Pos() {
    override fun toString(): String = "($source:$line:$col)"
  }
}

sealed class PrimitiveNode : Node {
  abstract val value: Any?
  override fun atIndex(index: Int): Node = UndefinedNode(pos, "$dotpath$[$index]")
  override fun atKey(key: String): Node = UndefinedNode(pos, "$dotpath.$key")
}

sealed class NumberNode : PrimitiveNode()

data class StringNode(override val value: String, override val pos: Pos, override val dotpath: String) : PrimitiveNode() {
  override val simpleName: String = "string"
}

data class BooleanNode(override val value: Boolean,
                       override val pos: Pos,
                       override val dotpath: String) : PrimitiveNode() {
  override val simpleName: String = "boolean"
}

data class LongNode(override val value: Long, override val pos: Pos, override val dotpath: String) : NumberNode() {
  override val simpleName: String = "long"
}

data class DoubleNode(override val value: Double, override val pos: Pos, override val dotpath: String) : NumberNode() {
  override val simpleName: String = "double"
}

data class NullNode(override val pos: Pos, override val dotpath: String) : PrimitiveNode() {
  override val value: Any? = null
  override val simpleName: String = "null"
}

data class UndefinedNode(override val pos: Pos, override val dotpath: String) : Node {
  override val simpleName: String = "undefined"
  override fun atKey(key: String): Node = this
  override fun atIndex(index: Int): Node = this
}

sealed class ContainerNode : Node

data class MapNode(val map: Map<String, Node>, override val pos: Pos, override val dotpath: String) : ContainerNode() {
  override val simpleName: String = "map"
  override fun atKey(key: String): Node = get(key)
  override fun atIndex(index: Int): Node = UndefinedNode(pos, "$dotpath$[$index]")
  operator fun get(key: String): Node = map.getOrDefault(key, UndefinedNode(pos, "$dotpath.$key"))
}

data class ListNode(val elements: List<Node>, override val pos: Pos, override val dotpath: String) : ContainerNode() {
  override val simpleName: String = "list"
  override fun atKey(key: String): Node = UndefinedNode(pos, "$dotpath.$key")
  override fun atIndex(index: Int): Node = elements.getOrElse(index) { UndefinedNode(pos, "$dotpath$[$index]") }
  operator fun get(index: Int): Node = atIndex(index)

}
