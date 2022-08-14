@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.withMeta

/**
 * Replaces strings of the form ${var} with the value of the env variable or system property 'var'.
 * Defaults can also be applied in case the env var is not available: ${var:-default}.
 *
 * If a replacement var is not available, then this preprocessor will throw an error.
 */
object EnvOrSystemPropertyPreprocessor : TraversingPrimitivePreprocessor() {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(.*?)\\}".toRegex()
  private val valueWithDefaultRegex = "(.*?):-(.*?)".toRegex()

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> {
      val rawValue = node.value
      val value = regex.replace(rawValue) { match ->
        val key = match.groupValues[1]
        when (val matchWithDefault = valueWithDefaultRegex.matchEntire(key)) {
          null -> System.getProperty(key) ?: System.getenv(key) ?: match.value
          // lookup with default value fallback
          else -> matchWithDefault.let { m ->
            val key2 = m.groupValues[1]
            val default = m.groupValues[2]
            System.getProperty(key2) ?: System.getenv(key2) ?: default
          }
        }
      }
      node.copy(value = value).withMeta(CommonMetadata.UnprocessedValue, rawValue).valid()
    }
    else -> node.valid()
  }
}
