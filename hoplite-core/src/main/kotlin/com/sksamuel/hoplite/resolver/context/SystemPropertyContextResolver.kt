package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid

/**
 * Replaces strings of the form ${{ sysprop:name }} by looking up the name as a system property.
 * Defaults can also be applied in case the system property does not exist: ${{ sysprop:name :- default }}
 */
object SystemPropertyContextResolver : ContextResolver() {

  override val contextKey = "sysprop"
  override val default = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    // System.getProperty throws IllegalArgumentException on an empty key, which can happen for a
    // placeholder like `${{ sysprop: }}` or `${{ sysprop:   }}`. Treat an empty key as "not set"
    // so the resolver's :- fallback path engages or an unresolved-substitution failure is reported,
    // rather than crashing the loader.
    return if (path.isEmpty()) null.valid() else System.getProperty(path).valid()
  }

}
