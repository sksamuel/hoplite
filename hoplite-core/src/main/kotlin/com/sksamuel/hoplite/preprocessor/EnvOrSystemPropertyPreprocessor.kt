@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid

/**
 * Replaces strings of the form ${var} with the value of the env variable 'var' or system property 'var'.
 * Defaults can also be applied in case the env var is not available: ${var:-default}.
 *
 * The negative-lookahead `(?!\{)` skips `${{...}}` placeholders so this legacy preprocessor does not
 * mangle context-resolver syntax such as `${{ env:VAR :- fallback }}` (gh-472), which the regex would
 * otherwise greedily match through the inner `{` and the first `}`.
 */
object EnvOrSystemPropertyPreprocessor : TraversingPrimitivePreprocessor() {

  // Redundant escaping required for Android support.
  private val regex = "\\$\\{(?!\\{)(.*?)\\}".toRegex()
  private val valueWithDefaultRegex = "(.*?):-(.*?)".toRegex()

  // System.getProperty throws IllegalArgumentException when called with an empty key,
  // and that has crashed users whose configs end up containing a literal ${} placeholder
  // (gh-469). Skip the lookup when the key is empty.
  private fun lookup(key: String): String? =
    if (key.isEmpty()) null else System.getProperty(key) ?: System.getenv(key)

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      val rawValue = node.value
      val value = regex.replace(rawValue) { match ->
        val key = match.groupValues[1]
        when (val matchWithDefault = valueWithDefaultRegex.matchEntire(key)) {
          null -> lookup(key) ?: match.value
          // lookup with default value fallback
          else -> matchWithDefault.let { m ->
            val key2 = m.groupValues[1]
            val default = m.groupValues[2]
            lookup(key2) ?: default
          }
        }
      }
      if (value == node.value) node.valid() else node.copy(value = value).valid()
    }
    else -> node.valid()
  }
}
