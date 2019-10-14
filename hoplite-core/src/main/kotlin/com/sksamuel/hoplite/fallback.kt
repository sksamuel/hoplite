package com.sksamuel.hoplite

fun Node.fallback(other: Node): Node {
  return when (val self = this) {
    is Undefined -> other
    is MapNode -> when (other) {
      is MapNode -> {
        val keys = self.map.keys + other.map.keys
        val map = keys.associateWith { self.atKey(it).fallback(other.atKey(it)) }
        MapNode(map, self.pos, self.value.fallback(other.value))
      }
      else -> self
    }
    else -> self
  }
}
