@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${{ environment }} by using the supplied [com.sksamuel.hoplite.env.Environment] value.
 * Defaults can also be applied in case the env var does not exist: ${{ environment :- default }}
 */
object EnvironmentResolver : RegexResolverWithDefault() {

  // redundant escaping required for Android support
  // this regex will match most nested replacements first (inside to outside)
  override val regex = "\\$\\{\\{\\s*env\\.([^{}]*)\\}\\}".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode> {
    return (context.environment?.name ?: "unknown") to node
  }

}
