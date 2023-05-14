@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${{ sysprop.name }} by looking up the name as a system property.
 * Defaults can also be applied in case the system property does not exist: ${{ sysprop.name :- default }}
 */
object SystemPropertyResolver : RegexResolverWithDefault() {

  // redundant escaping required for Android support
  // this regex will match most nested replacements first (inside to outside)
  override val regex = "\\$\\{\\{\\s*sysprop\\.([^{}]*)\\}\\}".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode> {
    return System.getProperty(path) to node
  }

}
