package com.sksamuel.hoplite.parsers

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath
import java.util.Properties

fun Properties.toNode(
  source: String,
  delimiter: String = ".",
  keyExtractor: (Any) -> String = { it.toString() },
) = asIterable().toNode(
  source = source,
  sourceKeyExtractor = { it.key },
  keyExtractor = keyExtractor,
  valueExtractor = { it.value },
  delimiter = delimiter
)

fun <T : Any> Map<String, T?>.toNode(
  source: String,
  delimiter: String = ".",
  keyExtractor: (String) -> String = { it },
) = entries.toNode(
  source = source,
  sourceKeyExtractor = { it.key },
  keyExtractor = keyExtractor,
  valueExtractor = { it.value },
  delimiter = delimiter
)

data class Element(
  val values: MutableMap<String, Element> = hashMapOf(),
  var value: Any? = null,
  var sourceKey: String? = null,
)

private fun <T, K> Iterable<T>.toNode(
  source: String,
  sourceKeyExtractor: (T) -> K,
  keyExtractor: (K) -> String,
  valueExtractor: (T) -> Any?,
  delimiter: String = "."
): Node {
  val map = Element()

  forEach { item ->
    val sourceKey = sourceKeyExtractor(item)
    val key = keyExtractor(sourceKey)
    val value = valueExtractor(item)
    val segments = key.split(delimiter)

    segments.foldIndexed(map) { index, element, segment ->
      element.values.getOrPut(segment) { Element() }.also {
        if (index == segments.size - 1) {
          it.value = value
          it.sourceKey = sourceKey.toString()
        }
      }
    }
  }

  val pos = Pos.SourcePos(source)

  fun Any.transform(path: DotPath, parentSourceKey: String? = null): Node = when (this) {
    is Element -> when {
      value != null && values.isEmpty() -> value?.transform(path, sourceKey) ?: Undefined
      else -> MapNode(
        map = values.takeUnless { it.isEmpty() }?.mapValues { it.value.transform(path.with(it.key), sourceKey) }.orEmpty(),
        pos = pos,
        path = path,
        value = value?.transform(path, sourceKey) ?: Undefined,
      )
    }
    is Array<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path, parentSourceKey) },
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
    )
    is Collection<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path, parentSourceKey) },
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
    )
    is Map<*, *> -> MapNode(
      map = takeUnless { it.isEmpty() }?.mapNotNull { entry ->
        val key = entry.key.toString()
        entry.value?.let { key to it.transform(path.with(key), key) }
      }?.toMap().orEmpty(),
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
    )
    else -> StringNode(this.toString(), pos, path = path, emptyMap(), parentSourceKey)
  }

  return map.transform(DotPath.root)
}
