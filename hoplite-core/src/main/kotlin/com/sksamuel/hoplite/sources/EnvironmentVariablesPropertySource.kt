package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

class EnvironmentVariablesPropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val allowUppercaseNames: Boolean,
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
  private val prefix: String? = null, // optional prefix to strip from the vars
) : PropertySource {

  override fun source(): String = "Env Var"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = environmentVariableMap()
      .mapKeys { if (prefix == null) it.key else it.key.removePrefix(prefix) }

    return map.toNode("env") { key ->
      key
        .let { if (prefix == null) it else it.removePrefix(prefix) }
        .let { if (useUnderscoresAsSeparator) it.replace("__", ".") else it }
        .let {
          if (allowUppercaseNames && Character.isUpperCase(it.codePointAt(0))) {
            it.split(".").joinToString(separator = ".") { value ->
              value.fold("") { acc, char ->
                when {
                  acc.isEmpty() -> acc + char.lowercaseChar()
                  acc.last() == '_' -> acc.dropLast(1) + char.uppercaseChar()
                  else -> acc + char.lowercaseChar()
                }
              }
            }
          } else {
            it
          }
        }
    }.valid()
  }
}
