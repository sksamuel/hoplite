package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath
import java.util.Properties

@Suppress("UNCHECKED_CAST")
fun Properties.toNode(source: String, delimiter: String = ".") = asIterable().toNode(
  source = source,
  keyExtractor = { it.key.toString() },
  valueExtractor = { it.value },
  delimiter = delimiter,
)

@Suppress("UNCHECKED_CAST")
fun <T : Any> Map<String, T?>.toNode(source: String, delimiter: String = ".") = entries.toNode(
  source = source,
  keyExtractor = { it.key },
  valueExtractor = { it.value },
  delimiter = delimiter,
)

data class Element(
  val values: MutableMap<String, Element> = hashMapOf(),
  var value: Any? = null,
)

@Suppress("UNCHECKED_CAST")
private fun <T> Iterable<T>.toNode(
  source: String,
  keyExtractor: (T) -> String,
  valueExtractor: (T) -> Any?,
  delimiter: String = "."
): Node {
  val map = Element()

  forEach { item ->
    val key = keyExtractor(item)
    val value = valueExtractor(item)
    val segments = key.split(delimiter)

    segments.foldIndexed(map) { index, element, segment ->
      element.values.computeIfAbsent(segment) { Element() }.also {
        if (index == segments.size - 1) it.value = value
      }
    }
  }

  val pos = Pos.SourcePos(source)

  fun Any.transform(path: DotPath): Node = when (this) {
    is Element -> when {
      value != null && values.isEmpty() -> value?.transform(path) ?: Undefined
      else -> MapNode(
        map = values.takeUnless { it.isEmpty() }?.mapValues { it.value.transform(path.with(it.key)) } ?: emptyMap(),
        pos = pos,
        path = path,
        value = value?.transform(path) ?: Undefined,
      )
    }
    is Array<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path) },
      pos = pos,
      path = path,
    )
    is Collection<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path) },
      pos = pos,
      path = path,
    )
    is Map<*, *> -> MapNode(
      map = takeUnless { it.isEmpty() }?.mapNotNull { entry ->
        entry.value?.let { entry.key.toString() to it.transform(path.with(entry.key.toString())) }
      }?.toMap() ?: emptyMap(),
      pos = pos,
      path = path,
    )
    else -> StringNode(this.toString(), pos, path = path, emptyMap())
  }

  return map.transform(DotPath.root)
}
