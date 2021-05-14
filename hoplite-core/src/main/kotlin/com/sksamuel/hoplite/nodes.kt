package com.sksamuel.hoplite

interface Node {

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
  fun atKey(key: String): Node

  operator fun get(key: String): Node = atKey(key)

  /**
   * Returns the [Node] stored at the given index of this node.
   *
   * Returns [Undefined] if this node does not contain an
   * element at the given index, or is not an [ArrayNode].
   */
  fun atIndex(index: Int): Node

  operator fun get(index: Int): Node = atIndex(index)

  /**
   * Returns the [Node] at the given path, by recursivel calling [atKey]
   * for each dot seperated element in the input path.
   */
  fun atPath(path: String): Node {
    val parts = path.split('.')
    return parts.fold(this) { acc, part -> acc.atKey(part) }
  }

  val simpleName: String

  /**
   * Returns he number of child nodes this node contains. That is, for [ArrayNode] it is
   * the number of child nodes; for [MapNode] it is the number of fields, and for all
   * other nodes it is 0.
   */
  val size: Int
}

val Node.isDefined: Boolean
  get() = this !is Undefined

fun Node.hasKeyAt(key: String): Boolean = atKey(key).isDefined

/**
 * Applies the given function to all string values, recursively calling into lists and maps.
 */
fun Node.transform(f: (String) -> String): Node = when (this) {
  is StringNode -> this.copy(value = f(this.value))
  is MapNode -> MapNode(map.map { f(it.key) to it.value.transform(f) }.toMap(), pos, this.value)
  is ArrayNode -> ArrayNode(elements.map { it.transform(f) }, pos)
  else -> this
}

/**
 * Applies the given function to all key names, recursively calling into lists and maps.
 */
fun Node.mapKey(f: (String) -> String): Node = when (this) {
  is MapNode -> this.copy(map = this.map.map { f(it.key) to it.value.mapKey(f) }.toMap())
  else -> this
}

sealed class ContainerNode : Node

data class MapNode(val map: Map<String, Node>,
                   override val pos: Pos,
                   val value: Node = Undefined) : ContainerNode() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): Node = map[key] ?: Undefined
  override fun atIndex(index: Int): Node = Undefined
  override val size: Int = map.size
}

data class ArrayNode(val elements: List<Node>,
                     override val pos: Pos) : ContainerNode() {
  override val simpleName: String = "List"
  override fun atKey(key: String): Node = Undefined
  override fun atIndex(index: Int): Node = elements.getOrElse(index) { Undefined }
  override val size: Int = elements.size
}

sealed class PrimitiveNode : Node {
  override fun atKey(key: String): Node = Undefined
  override fun atIndex(index: Int): Node = Undefined
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

data class NullNode(override val pos: Pos) : PrimitiveNode() {
  override val simpleName: String = "null"
  override val value: Any? = null
}

object Undefined : Node {
  override val simpleName: String = "Undefined"
  override val pos: Pos = Pos.NoPos
  override fun atKey(key: String): Node = this
  override fun atIndex(index: Int): Node = this
  override val size: Int = 0
}
