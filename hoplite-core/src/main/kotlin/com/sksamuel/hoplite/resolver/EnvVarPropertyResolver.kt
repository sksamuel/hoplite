@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${{ env.name }} by looking up the name as an environment variable.
 * Defaults can also be applied in case the env var does not exist: ${{ env.name :- default }}
 */
object EnvVarPropertyResolver : RegexResolverWithDefault() {

  // redundant escaping required for Android support
  // this regex will match most nested replacements first (inside to outside)
  override val regex = "\\$\\{\\{\\s*env\\.([^{}]*)\\}\\}".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode> {
    return System.getenv(path) to node
  }

}
