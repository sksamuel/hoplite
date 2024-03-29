package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

class EnvironmentVariablesPropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val useSingleUnderscoresAsSeparator: Boolean,
  private val allowUppercaseNames: Boolean,
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
  private val prefix: String? = null, // optional prefix to strip from the vars
) : PropertySource {

  override fun source(): String = "Env Var"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = environmentVariableMap()
      .mapKeys { if (prefix == null) it.key else it.key.removePrefix(prefix) }

    // at the moment the delimiter is either `__` or `.` -- it can't be mixed
    val delimiter = if (useUnderscoresAsSeparator) "__" else if (useSingleUnderscoresAsSeparator) "_" else "."

    return map.toNode("env", delimiter) { key ->
      key
        .let { if (prefix == null) it else it.removePrefix(prefix) }
        .let {
          if (allowUppercaseNames && Character.isUpperCase(it.codePointAt(0))) {
            it.split(delimiter).joinToString(separator = delimiter) { value ->
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
