package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import java.util.Properties

class EnvironmentVariablesPropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val allowUppercaseNames: Boolean,
  private val environmentVariableMap: () -> Map<String, String> = { System.getenv() },
) : PropertySource {

  override fun source(): String = "Env Var"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val props = Properties()
    environmentVariableMap().forEach {
      val key = it.key
        .let { key -> if (useUnderscoresAsSeparator) key.replace("__", ".") else key }
        .let { key ->
          if (allowUppercaseNames && Character.isUpperCase(key.codePointAt(0))) {
            key.split(".").joinToString(separator = ".") { value ->
              value.fold("") { acc, char ->
                when {
                  acc.isEmpty() -> acc + char.lowercaseChar()
                  acc.last() == '_' -> acc.dropLast(1) + char.uppercaseChar()
                  else -> acc + char.lowercaseChar()
                }
              }
            }
          } else {
            key
          }
        }
      props[key] = it.value
    }
    return props.toNode("env").valid()
  }
}
