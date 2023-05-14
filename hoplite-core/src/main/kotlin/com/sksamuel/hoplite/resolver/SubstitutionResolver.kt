@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode

/**
 * Replaces strings of the form ${{ path }} by looking up the path in the parsed config.
 * Defaults can also be applied in case the path does not exist: ${{ path:-default }}
 *
 * Note: This resolver will greedily accept inputs meant for the [EnvVarPropertyResolver] and
 * the [SystemPropertyResolver]. Therefore, this should always be registered after those.
 */
object SubstitutionResolver : RegexResolverWithDefault() {

  // redundant escaping required for Android support
  // this regex will match most nested replacements first (inside to outside)
  override val regex = "\\$\\{\\{([^{}]*)\\}\\}".toRegex()

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode> =
    when (val n = root.atPath(path)) {
      is StringNode -> n.value to node
      is LongNode -> n.value.toString() to node
      is DoubleNode -> n.value.toString() to node
      is BooleanNode -> n.value.toString() to node
      else -> "null" to node
    }
}
