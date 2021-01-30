package com.sksamuel.hoplite

/**
 * Returns a new [Node] which is the result of merging this node, with keys from the given node,
 * with keys in this node taking precedence.
 */
internal fun Node.merge(other: Node): Node {
  return when (this) {
    is Undefined -> other
    is MapNode -> when (other) {
      is MapNode -> {
        val keys = this.map.keys + other.map.keys
        val map = keys.associateWith { this.atKey(it).merge(other.atKey(it)) }
        MapNode(map, this.pos, this.value.merge(other.value))
      }
      else -> this
    }
    else -> this
  }
}
