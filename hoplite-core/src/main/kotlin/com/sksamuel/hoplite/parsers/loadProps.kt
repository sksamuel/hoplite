package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.TreeNode
import com.sksamuel.hoplite.Pos
import java.util.*

@Suppress("UNCHECKED_CAST")
fun loadProps(props: Properties, source: String): TreeNode {

  val root = mutableMapOf<String, Any>()
  props.stringPropertyNames().toList().map { key ->

    val components = key.split('.')
    val map = components.fold(root) { acc, k ->
      acc.getOrPut(k) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
    }
    map.put("____value", props.getProperty(key))
  }

  val pos = Pos.FilePos(source)

  fun Map<String, Any>.toNode(): MapNode {
    val maps = filterValues { it is MutableMap<*, *> }.mapValues {
      when (val v = it.value) {
        is MutableMap<*, *> -> (v as MutableMap<String, Any>).toNode()
        else -> throw java.lang.RuntimeException("Bug: unsupported state $it")
      }
    }
    val value = this["____value"]
    return MapNode(maps.toMap(), pos, value)
  }

  return root.toNode()
}
