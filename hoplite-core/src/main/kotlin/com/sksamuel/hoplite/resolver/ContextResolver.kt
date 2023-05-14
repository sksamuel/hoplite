@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

/**
 * A [ContextResolver] applies substitutions to context patterns in a [StringNode]'s value.
 *
 * Context patterns are of the form ${{ context.path }} where `context` indicates the context
 * resolver to use, and `path` is provided to that resolver for runtime resolution.
 *
 * For example, the [EnvVarResolver] will replace ${{ env.name }} with the env variable `name`,
 * and the [SystemPropertyResolver] will replace ${{ sysprop.name }} with the system property `name`.
 *
 * If the supplied context or path cannot be resolved, an error will be returned if the
 * [ContextResolverMode] is set to [ContextResolverMode.Error] (which is the default). To disable
 * errors, set this value to [ContextResolverMode.Silent].
 */
abstract class ContextResolver : Resolver {

  abstract val contextKey: String
  abstract val default: Boolean

  // this regex will match most nested replacements first (inside to outside)
  // redundant escaping required for Android support
  fun regex() = "\\$\\{\\{\\s*$contextKey://([^{}]*)\\}\\}".toRegex()

  private val valueWithDefaultRegex = "(.+):-(.+)".toRegex()

  /**
   * Return the replacement value, or null if no replacement should take place.
   */
  abstract fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?>

  private fun lookupWithFallback(
    path: String,
    fallback: String,
    node: StringNode,
    root: Node,
    context: DecoderContext
  ) = lookup(path, node, root, context).flatMap { it?.valid() ?: lookup(fallback, node, root, context) }

  override suspend fun resolve(node: Node, root: Node, context: DecoderContext): ConfigResult<Node> {
    return when (node) {
      is StringNode -> resolve(node, root, context)
      else -> node.valid()
    }
  }

  private fun resolve(node: StringNode, root: Node, context: DecoderContext): ConfigResult<StringNode> {

    val result = regex().find(node.value) ?: return node.valid()
    val path = result.groupValues[1].trim()

    val matchWithDefault = valueWithDefaultRegex.matchEntire(path)
    val replacement = when {
      matchWithDefault == null -> lookup(path.trim(), node, root, context)
      !default -> lookup(path.trim(), node, root, context)
      // default value provided, so we use the first component with second component fallback
      else -> lookupWithFallback(
        matchWithDefault.groupValues[1].trim(), matchWithDefault.groupValues[2].trim(), node, root, context
      )
    }

    return replacement.flatMap {
      when {
        it == null && context.contextResolverMode == ContextResolverMode.Silent -> node.valid()
        it == null -> ConfigFailure.ResolverError("Could not resolve '$path'").invalid()
        else -> node.copy(value = node.value.replaceRange(result.range, it)).valid()
      }
    }
  }
}
