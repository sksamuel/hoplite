package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import java.util.Properties

/**
 * This [PropertySource] is designed to allow for temporary override of values.
 *
 * It looks for any env variables which begin with the prefix `config.override`, and then
 * makes those available.
 */
class EnvironmentVariableOverridePropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() }
) : PropertySource {

  private val Prefix = "config.override."

  override fun source(): String = "Env Var Overrides"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val vars = environmentVariableMap()
      .mapKeys { if (useUnderscoresAsSeparator) it.key.replace("__", ".") else it.key }
      .filter { it.key.startsWith(Prefix) }
    return if (vars.isEmpty()) Undefined.valid() else {
      vars.toNode("Env Var Overrides") {
        it.removePrefix(Prefix)
      }.valid()
    }
  }
}
