package com.sksamuel.hoplite

/**
 * Returns a new [Node] which is the result of merging this node with keys from the [other] node,
 * with keys in this node taking preccence if they are defined and not null.
 */
internal fun Node.merge(other: Node): Node {
  require(this.path == other.path)
  return when (this) {
    is Undefined -> other
    // if I am null, and the other is defined, then that takes precedence
    is NullNode -> when (other) {
      is Undefined -> this
      else -> other
    }
    is MapNode -> when (other) {
      // if both are maps we merge
      is MapNode -> {
        val keys = this.map.keys + other.map.keys
        val map = keys.associateWith { this.atKey(it).merge(other.atKey(it)) }
        MapNode(map, this.pos, this.path, this.value.merge(other.value))
      }
      else -> this
    }
    // if I am a non-null map then I take precedence
    else -> this
  }
}
