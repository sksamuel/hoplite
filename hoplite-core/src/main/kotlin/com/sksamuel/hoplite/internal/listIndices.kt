package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode

/**
 * True if this map's keys are all list indices, as sources of flat keys (environment variables, system properties,
 * command line arguments, maps) give a list: `databases.0.host`, `databases.1.host`, ... (gh-358).
 */
internal fun MapNode.isListIndexMap(): Boolean =
  map.isNotEmpty() && map.keys.all { key -> key.isNotEmpty() && key.all { it in '0'..'9' } && key.toIntOrNull() != null }

/**
 * This map keyed by list indices as a list of its values in index order, closing up any gaps between indices, for
 * a collection decoder. The elements are marked as used, as their paths (keyed by index) are not the list's own.
 */
internal fun MapNode.toIndexedArray(context: DecoderContext): ArrayNode {
  map.values.forEach { context.usedPaths.add(it.path) }
  return ArrayNode(
    map.entries.sortedBy { it.key.toInt() }.map { it.value },
    pos, path, meta - LookupTreeBuilder.LIST_INDICES, delimiter, sourceKey,
  )
}
