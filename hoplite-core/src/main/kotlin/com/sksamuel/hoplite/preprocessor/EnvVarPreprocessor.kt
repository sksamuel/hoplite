package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

object EnvVarPreprocessor : Preprocessor {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(.*?)\\}".toRegex()
  private val valueWithDefaultRegex = "(.*?):-(.*?)".toRegex()

  override fun process(node: Node): Node = when (node) {
    is StringNode -> {
      val value = regex.replace(node.value) {
        val key = it.groupValues[1]
        when (val matchWithDefault = valueWithDefaultRegex.matchEntire(key)) {
          null -> System.getenv(key) ?: it.value
          // lookup with default value fallback
          else -> matchWithDefault.let { m -> System.getenv(m.groups[1]!!.value) ?: m.groups[2]!!.value }
        }
      }
      node.copy(value = value)
    }
    else -> node
  }
}
