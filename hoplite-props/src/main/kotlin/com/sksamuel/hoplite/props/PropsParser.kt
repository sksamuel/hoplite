package com.sksamuel.hoplite.props

import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Parser
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import java.io.InputStream
import java.util.*

@Suppress("UNCHECKED_CAST")
class PropsParser : Parser {

  private fun MutableMap<String, Any>.toNode(pos: Pos, path: String): MapNode =
    MapNode(this.toMap().mapValues {
      when (val v = it.value) {
        is MutableMap<*, *> -> (v as MutableMap<String, Any>).toNode(pos, "$path.${it.key}")
        is StringNode -> v.copy(dotpath = "$path.${it.key}")
        else -> throw java.lang.RuntimeException("Bug: unsupported state $it")
      }
    }, pos, path)

  override fun load(input: InputStream, source: String): Node {
    val props = Properties()
    props.load(input)
    val root = mutableMapOf<String, Any>()
    props.propertyNames().toList().map {
      val key = it.toString()
      val components = key.split('.')
      val value = props.getProperty(key)
      val leaf = components.dropLast(1).fold(root) { map, k ->
        map.getOrPut(k, { mutableMapOf<String, Any>() }) as MutableMap<String, Any>
      }
      if (leaf.containsKey(components.last())) throw RuntimeException("Props contains duplicate leaf key ${components.last()}")
      leaf.put(components.last(), StringNode(value, Pos.FilePos(source), ""))
    }
    return root.toNode(Pos.FilePos(source), "<root>")
  }

  override fun defaultFileExtensions(): List<String> = listOf("props")
}
