package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${var} with the value of the env variable 'var'.
 * Defaults can also be applied in case the env var is not available: ${var:-default}.
 */
object EnvVarPreprocessor : TraversingPrimitivePreprocessor() {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(.*?)\\}".toRegex()
  private val valueWithDefaultRegex = "(.*?):-(.*?)".toRegex()

  override fun handle(node: PrimitiveNode): Node = when (node) {
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
