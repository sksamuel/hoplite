package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecodedPath
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

/**
 * Checks results based on [DecodeMode].
 */
class DecodeModeValidator(private val mode: DecodeMode) {

  fun <A : Any> validate(a: A, state: DecodingState): ConfigResult<A> {
    return when (mode) {
      DecodeMode.Strict -> ensureAllUsed(state).map { a }
      DecodeMode.Lenient -> a.valid()
    }
  }

  private fun ensureAllUsed(result: DecodingState): ConfigResult<DecodingState> {
    val unused = result.unused.filterNot { it.isExternalSource() }
    return if (unused.isEmpty()) result.valid() else {
      val errors = NonEmptyList.unsafe(unused.map { ConfigFailure.UnusedPath(it) })
      ConfigFailure.MultipleFailures(errors).invalid()
    }
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
