@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid

/**
 * Replaces strings of the form ${{ hoplite:environment }} by using the [com.sksamuel.hoplite.env.Environment] value
 * provided to the config loader. Defaults can also be applied in case the environment does not
 * exist, eg: ${{ hoplite:environment :- default }}
 */
object HopliteContextResolver : ContextResolver() {

  override val contextKey: String = "hoplite"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return context.environment?.name.valid()
  }
}
