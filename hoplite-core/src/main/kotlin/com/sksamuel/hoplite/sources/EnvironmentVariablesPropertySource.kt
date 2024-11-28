package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

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

    return map.toNode("env", DELIMITER).valid()
  }
}
