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
   * Returns the [Node] stored at the given key of this value.
   */
  fun atKey(key: String): Node

  /**
   * Returns the [Node] stored at the index of this value
   */
  fun atIndex(index: Int): Node

  fun atPath(path: String): Node {
    val parts = path.split('.')
    return parts.fold(this, { acc, part -> acc.atKey(part) })
  }

  /**
   * Returns the underlying [Node] as a String, if the value is
   * a [StringNode] type, otherwise returns an error.
   */
  fun string(): ConfigResult<String> = when (this) {
    is StringNode -> this.value.validNel()
    else -> ConfigResults.failedTypeConversion(this)
  }

  fun transform(f: (String) -> String): Node = when (this) {
    is StringNode -> StringNode(f(value), pos)
    is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos)
    is ListNode -> ListNode(elements.map { it.transform(f) }, pos)
    else -> this
  }

  fun withFallback(fallback: Node): Node = object : Node {
    override val pos: Pos = this@Node.pos
    override fun atKey(key: String): Node = this@Node.atKey(key).recover(fallback.atKey(key))
    override fun atIndex(index: Int): Node = this@Node.atIndex(index).recover(fallback.atIndex(index))
  }

  fun recover(node: Node): Node = when (this) {
    is UndefinedNode -> node
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

sealed class PrimitiveNode : Node {
  abstract val value: Any?
  override fun atIndex(index: Int): Node = UndefinedNode(pos)
  override fun atKey(key: String): Node = UndefinedNode(pos)
}

sealed class NumberNode : PrimitiveNode()

data class StringNode(override val value: String, override val pos: Pos) : PrimitiveNode()
data class BooleanNode(override val value: Boolean, override val pos: Pos) : PrimitiveNode()
data class LongNode(override val value: Long, override val pos: Pos) : NumberNode()
data class DoubleNode(override val value: Double, override val pos: Pos) : NumberNode()
data class NullNode(override val pos: Pos) : PrimitiveNode() {
  override val value: Any? = null
}

data class UndefinedNode(override val pos: Pos) : Node {
  override fun atKey(key: String): Node = this
  override fun atIndex(index: Int): Node = this
}

sealed class ContainerNode : Node

data class MapNode(val map: Map<String, Node>, override val pos: Pos) : ContainerNode() {
  override fun atKey(key: String): Node = get(key)
  override fun atIndex(index: Int): Node = UndefinedNode(pos)
  operator fun get(key: String): Node = map.getOrDefault(key, UndefinedNode(pos))
}

data class ListNode(val elements: List<Node>, override val pos: Pos) : ContainerNode() {
  override fun atKey(key: String): Node = UndefinedNode(pos)
  override fun atIndex(index: Int): Node = elements.getOrElse(index) { UndefinedNode(pos) }
  operator fun get(index: Int): Node = atIndex(index)

}
