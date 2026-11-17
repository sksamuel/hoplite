package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.internal.LookupTreeBuilder
import com.sksamuel.hoplite.internal.isListIndexMap
import com.sksamuel.hoplite.parsers.toNode
import com.sksamuel.hoplite.transform
import com.sksamuel.hoplite.withMeta

class EnvironmentVariablesPropertySource(
  internal val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
  /** Optional prefix to limit env var selection. It is stripped before processing. */
  internal val prefix: String? = null,
) : PropertySource {
  companion object {
    const val DELIMITER = "_"
  }

  override fun source(): String = "Env Var"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = environmentVariableMap().run {
      if (prefix == null) {
        this
      } else {
        mapNotNull { (key, value) ->
          key.removePrefix(prefix).takeIf { it != key }?.let { it to value }
        }.toMap()
      }
    }

    // Without a prefix, the source sees every variable in the process environment, so it is ambient (#606).
    val ambient = prefix.isNullOrEmpty()

    return map.toNode("env", DELIMITER).transform { node ->
      val transformed = if (node is MapNode && node.isListIndexMap()) {
        // All the map keys are integer index strings, so this is a list. It stays a map keyed by index through
        // the cascade, so that each index can set the element at that index of a list from a lower-precedence
        // source (gh-358), and becomes an array, in index order, after the cascade.
        node.withMeta(LookupTreeBuilder.LIST_INDICES, true)
      } else {
        node
      }
      if (ambient) transformed.withMeta(CommonMetadata.Ambient, true) else transformed
    }.valid()
  }
}
