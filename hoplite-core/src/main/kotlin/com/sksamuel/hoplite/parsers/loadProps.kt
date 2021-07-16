package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.*
import java.util.*

@Suppress("UNCHECKED_CAST")
fun Properties.toNode(source: String) = asIterable().toNode(
  source = source,
  keyExtractor = { it.key.toString() },
  valueExtractor = { it.value }
)

@Suppress("UNCHECKED_CAST")
fun <T : Any> Map<String, T?>.toNode(source: String) = entries.toNode(
  source = source,
  keyExtractor = { it.key },
  valueExtractor = { it.value },
)

data class Element(
  val values: MutableMap<String, Element> = hashMapOf(),
  var value: Any? = null,
)

@Suppress("UNCHECKED_CAST")
private fun <T> Iterable<T>.toNode(source: String, keyExtractor: (T) -> String, valueExtractor: (T) -> Any?): Node {
  val map = Element()

  forEach { item ->
    val key = keyExtractor(item)
    val value = valueExtractor(item)
    val segments = key.split(".")

    segments.foldIndexed(map) { index, element, segment ->
      element.values.computeIfAbsent(segment) { Element() }.also {
        if (index == segments.size - 1) it.value = value
      }
    }
  }

  val pos = Pos.FilePos(source)

  fun Any.transform(): Node = when (this) {
    is Element -> when {
      value != null && values.isEmpty() -> value?.transform() ?: Undefined
      else -> MapNode(
        map = values.takeUnless { it.isEmpty() }?.mapValues { it.value.transform() } ?: emptyMap(),
        value = value?.transform() ?: Undefined,
        pos = pos,
      )
    }
    is Array<*> -> ArrayNode(
      elements = mapNotNull { it?.transform() },
      pos = pos,
    )
    is Collection<*> -> ArrayNode(
      elements = mapNotNull { it?.transform() },
      pos = pos,
    )
    is Map<*, *> -> MapNode(
      map = takeUnless { it.isEmpty() }?.mapNotNull { entry ->
        entry.value?.let { entry.key.toString() to it.transform() }
      }?.toMap() ?: emptyMap(),
      pos = pos,
    )
    else -> StringNode(this.toString(), pos)
  }

  val result = map.transform()
  return result
}
