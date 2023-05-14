package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.valid

abstract class PrefixResolver : Resolver {

  abstract val prefix: String

  /**
   * Implementations should use the [path] to locate the appropriate replacement value, and return
   * a modified [node]. If the node should be unchanged, the original node can be returned.
   */
  abstract fun resolve(
    path: String,
    node: StringNode,
    root: Node,
    context: DecoderContext
  ): ConfigResult<StringNode>

  override suspend fun resolve(node: Node, root: Node, context: DecoderContext): ConfigResult<Node> {
    return when {
      node is StringNode && node.value.startsWith(prefix) -> {
        val path = node.value.removePrefix(prefix)
        resolve(path.trim(), node, root, context)
      }
      else -> node.valid()
    }
  }
}
