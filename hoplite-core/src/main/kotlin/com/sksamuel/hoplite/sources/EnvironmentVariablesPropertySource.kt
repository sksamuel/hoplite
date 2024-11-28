package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import com.sksamuel.hoplite.transform

class EnvironmentVariablesPropertySource(
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
  /** Optional prefix to limit env var selection. It is stripped before processing. */
  private val prefix: String? = null,
) : PropertySource {
  companion object {
    const val DELIMITER = "_"
  }

  override fun source(): String = "Env Var"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = environmentVariableMap()
      .filterKeys { if (prefix == null) true else it.startsWith(prefix) }
      .mapKeys { if (prefix == null) it.key else it.key.removePrefix(prefix) }

    return map.toNode("env", DELIMITER).transform { node ->
      if (node is MapNode && node.map.keys.all { it.toIntOrNull() != null }) {
        // all they map keys are ints, so lets transform the MapNode into an ArrayNode
        ArrayNode(node.map.values.toList(), node.pos, node.path, node.meta, node.delimiter, node.sourceKey)
      } else {
        node
      }
    }.valid()
  }
}
