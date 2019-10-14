package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Undefined
import java.util.*

@Suppress("UNCHECKED_CAST")
fun Properties.toNode(source: String): Node {

  val root = mutableMapOf<String, Any>()
  stringPropertyNames().toList().map { key ->

    val components = key.split('.')
    val map = components.fold(root) { acc, k ->
      acc.getOrPut(k) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
    }
    map.put("____value", getProperty(key))
  }

  val pos = Pos.FilePos(source)

  fun Map<String, Any>.toNode(): Node {
    val maps = filterValues { it is MutableMap<*, *> }.mapValues {
      when (val v = it.value) {
        is MutableMap<*, *> -> (v as MutableMap<String, Any>).toNode()
        else -> throw java.lang.RuntimeException("Bug: unsupported state $it")
      }
    }
    val value = this["____value"]
    return when {
      value == null && maps.isEmpty() -> throw java.lang.RuntimeException("Bug: unsupported state. value is null and map is empty")
      value == null && maps.isNotEmpty() -> MapNode(maps.toMap(), pos, Undefined)
      maps.isEmpty() -> StringNode(value.toString(), pos)
      else -> MapNode(maps.toMap(), pos, StringNode(value.toString(), pos))
    }
  }

  return root.toNode()
}
