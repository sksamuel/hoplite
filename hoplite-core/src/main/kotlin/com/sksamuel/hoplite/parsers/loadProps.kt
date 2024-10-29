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
  var delimiter: String = ".",
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
          it.delimiter = delimiter
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
        delimiter = delimiter,
      )
    }
    is Array<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path, parentSourceKey) },
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
      delimiter = delimiter,
    )
    is Collection<*> -> ArrayNode(
      elements = mapNotNull { it?.transform(path, parentSourceKey) },
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
      delimiter = delimiter,
    )
    is Map<*, *> -> MapNode(
      map = takeUnless { it.isEmpty() }?.mapNotNull { entry ->
        entry.value?.let { entry.key.toString() to it.transform(path.with(entry.key.toString()), parentSourceKey) }
      }?.toMap().orEmpty(),
      pos = pos,
      path = path,
      sourceKey = parentSourceKey,
      delimiter = delimiter,
    )
    else -> StringNode(this.toString(), pos, path = path, emptyMap(), delimiter, parentSourceKey)
  }

  return map.transform(DotPath.root)
}
