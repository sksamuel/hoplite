package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.DecodedPath
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

/**
 * Checks results based on [DecodeMode].
 */
class DecodeModeValidator(
  private val mode: DecodeMode,
  /**
   * If true (the default), strict mode exempts only nodes marked [CommonMetadata.Ambient] (unprefixed env vars, sysprops with an
   * empty prefix), and checks prefixed env and sysprops sources like any other (#606). If false, it exempts every
   * env var and system property (gh-505).
   */
  private val strictPrefixedSources: Boolean = true,
) {

  fun <A : Any> validate(a: A, state: DecodingState): ConfigResult<A> {
    return when (mode) {
      DecodeMode.Strict -> ensureAllUsed(state).map { a }
      DecodeMode.Lenient -> a.valid()
    }
  }

  private fun ensureAllUsed(result: DecodingState): ConfigResult<DecodingState> {
    val unused = if (strictPrefixedSources) {
      val ambient = ambientPaths(result.root)
      result.unused.filterNot { it.path in ambient }
    } else {
      result.unused.filterNot { it.isExternalSource() }
    }
    return if (unused.isEmpty()) result.valid() else {
      val errors = NonEmptyList.unsafe(unused.map { ConfigFailure.UnusedPath(it) })
      ConfigFailure.MultipleFailures(errors).invalid()
    }
  }

  // Paths of every node, branches included, that its source marked as ambient.
  private fun ambientPaths(node: Node): Set<DotPath> {
    val children = when (node) {
      is MapNode -> node.map.values + node.value
      is ArrayNode -> node.elements
      else -> emptyList()
    }
    val own = if (node.meta[CommonMetadata.Ambient] == true) setOf(node.path) else emptySet()
    return own + children.flatMap { ambientPaths(it) }
  }

  // Process-wide sources (environment variables, JVM system properties) typically contain values
  // the loader did not request — HOME, USER, TMPDIR, etc. for env vars, and arbitrary JVM
  // properties for sysprops. Reporting them as "unused" in strict mode produces noise that has
  // nothing to do with the user's config files (gh-505). Strict mode still catches stale values
  // in user-provided sources (yaml, json, hocon, props, map sources, ...).
  private fun DecodedPath.isExternalSource(): Boolean = when (pos.source()) {
    "env", "sysprops" -> true
    else -> false
  }
}

enum class DecodeMode {
  // errors if a config value is provided but not used
  Strict,

  // allows config to be unused
  Lenient
}
