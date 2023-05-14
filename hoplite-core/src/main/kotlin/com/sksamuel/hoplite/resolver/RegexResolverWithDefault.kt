package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

abstract class RegexResolverWithDefault : Resolver {

  private val valueWithDefaultRegex = "(.+):-(.+)".toRegex()

  abstract val regex: Regex

  abstract fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): Pair<String, StringNode>

  override suspend fun resolve(node: Node, root: Node, context: DecoderContext): ConfigResult<Node> {
    return when (node) {
      is StringNode -> resolve(node, root, context)
      else -> node.valid()
    }
  }

  private fun resolve(node: StringNode, root: Node, context: DecoderContext): ConfigResult<StringNode> {
    val result = regex.find(node.value) ?: return node.valid()
    val path = result.groupValues[1].trim()

    val replacement: String? = when (val matchWithDefault = valueWithDefaultRegex.matchEntire(path)) {
      // no default, so we use the entire match to lookup
      null -> lookup(path.trim(), node, root, context).first
      else -> {
        // default value provided, so we use the first component with second component fallback
        val fallback = matchWithDefault.groupValues[2].trim()
        lookup(matchWithDefault.groupValues[1].trim(), node, root, context).first ?: fallback
      }
    }

    return when {
      replacement == null -> ConfigFailure.UnresolvedSubstitution(path, root).invalid()
//      replacement == null -> node.valid()
      else -> node.copy(value = node.value.replaceRange(result.range, replacement)).valid()
    }
  }
}
