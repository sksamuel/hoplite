package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath

sealed interface Node {

  /**
   * Returns the positional information of this value.
   */
  val pos: Pos

  /**
   * Returns the path to this node from the root node.
   */
  val path: DotPath

  /**
   * The original source key of this node without any normalization.
   * Useful for reporting or potentially for custom decoders.
   */
  val sourceKey: String?

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

  /**
   * Arbitrary key value pairs that can be added to this node as metadata.
   */
  val meta: Map<String, Any?>
}

object CommonMetadata {
  const val Secret = "Secret"
  const val UnprocessedValue = "UnprocessedValue"
  const val RemoteLookup = "RemoteLookup"
}

/**
 * Returnes true if this node is not [Undefined]
 */
val Node.isDefined: Boolean
  get() = this !is Undefined

/**
 * Copies this node with the updated [path].
 */
fun Node.withPath(path: DotPath): Node = when (this) {
  is ArrayNode -> copy(path = path, elements = this.elements.map { it.withPath(path) })
  is MapNode -> copy(path = path, map = this.map.mapValues { it.value.withPath(path.with(it.key)) })
  is BooleanNode -> copy(path = path)
  is NullNode -> copy(path = path)
  is DoubleNode -> copy(path = path)
  is LongNode -> copy(path = path)
  is StringNode -> copy(path = path)
  Undefined -> Undefined
}

/**
 * Copies this node, adding in the given metadata key/value.
 */
fun <T : Node> T.withMeta(key: String, value: Any?): T = when (this) {
  is ArrayNode -> copy(meta = meta + Pair(key, value)) as T
  is MapNode -> copy(meta = meta + Pair(key, value)) as T
  is BooleanNode -> copy(meta = meta + Pair(key, value)) as T
  is NullNode -> copy(meta = meta + Pair(key, value)) as T
  is DoubleNode -> copy(meta = meta + Pair(key, value)) as T
  is LongNode -> copy(meta = meta + Pair(key, value)) as T
  is StringNode -> copy(meta = meta + Pair(key, value)) as T
  else -> Undefined as T
}

/**
 * Return all paths recursively in this tree.
 */
fun Node.paths(): Set<Pair<DotPath, Pos>> = setOf(this.path to this.pos) + when (this) {
  is ArrayNode -> this.elements.flatMap { it.paths() }
  is MapNode -> this.map.map { it.value.paths() }.flatten()
  else -> emptySet()
}

/**
 * Return all decoded paths recursively in this tree.
 */
fun Node.decodedPaths(): Set<DecodedPath> = setOf(DecodedPath(this.path, this.sourceKey, this.pos)) + when (this) {
  is ArrayNode -> this.elements.flatMap { it.decodedPaths() }
  is MapNode -> this.map.map { it.value.decodedPaths() }.flatten()
  else -> emptySet()
}

/**
 * Return all nodes in this tree, recursively transformed per the given transformer function.
 */
fun Node.transform(transformer: (Node) -> Node): Node = when (val transformed = transformer(this)) {
  is ArrayNode -> transformed.copy(elements = transformed.elements.map { it.transform(transformer) })
  is MapNode -> transformed.copy(
    map = transformed.map.mapValues { it.value.transform(transformer) },
    value = transformed.value.transform(transformer)
  )
  else -> transformed
}

sealed class ContainerNode : Node

data class MapNode(
  val map: Map<String, Node>,
  override val pos: Pos,
  override val path: DotPath,
  val value: Node = Undefined,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : ContainerNode() {
  override val simpleName: String = "Map"
  override fun atKey(key: String): Node = map[key] ?: Undefined
  override fun atIndex(index: Int): Node = Undefined
  override val size: Int = map.size
}

data class ArrayNode(
  val elements: List<Node>,
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : ContainerNode() {
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

data class StringNode(
  override val value: String,
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : PrimitiveNode() {
  override val simpleName: String = "String"
}

data class BooleanNode(
  override val value: Boolean,
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : PrimitiveNode() {
  override val simpleName: String = "Boolean"
}

sealed class NumberNode : PrimitiveNode()

data class LongNode(
  override val value: Long,
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : NumberNode() {
  override val simpleName: String = "Long"
}

data class DoubleNode(
  override val value: Double,
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : NumberNode() {
  override val simpleName: String = "Double"
}

data class NullNode(
  override val pos: Pos,
  override val path: DotPath,
  override val meta: Map<String, Any?> = emptyMap(),
  override val sourceKey: String? = if (path == DotPath.root) null else path.flatten(),
) : PrimitiveNode() {
  override val simpleName: String = "null"
  override val value: Any? = null
}

object Undefined : Node {
  override val simpleName: String = "Undefined"
  override val pos: Pos = Pos.NoPos
  override val path = DotPath.root
  override val sourceKey: String? = null
  override fun atKey(key: String): Node = this
  override fun atIndex(index: Int): Node = this
  override val size: Int = 0
  override val meta: Map<String, Any?> = emptyMap()
}

fun Node.valueOrNull(): String? = when (this) {
  is ArrayNode -> null
  is MapNode -> null
  is BooleanNode -> this.value.toString()
  is NullNode -> null
  is DoubleNode -> this.value.toString()
  is LongNode -> this.value.toString()
  is StringNode -> this.value
  Undefined -> null
}

/**
 * Traverse this node, returning a list of each intermediate node.
 */
fun Node.traverse(): List<Node> {
  return when (this) {
    is ArrayNode -> this.elements.flatMap { it.traverse() }
    is MapNode -> map.values.toList().flatMap { it.traverse() }
    Undefined -> emptyList()
    else -> listOf(this)
  }
}
