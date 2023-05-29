package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.valid

/**
 * Replaces strings of the form ${{ ref://path }} by looking up the path in the parsed config.
 * Defaults can also be applied in case the path does not exist: ${{ ref://path:-default }}
 */
object ReferenceContextResolver : ContextResolver() {

  override val contextKey = "ref"
  override val default = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> =
    when (val n = root.atPath(path)) {
      is StringNode -> n.value.valid()
      is LongNode -> n.value.toString().valid()
      is DoubleNode -> n.value.toString().valid()
      is BooleanNode -> n.value.toString().valid()
      else -> Validated.Valid(null)
    }
}
