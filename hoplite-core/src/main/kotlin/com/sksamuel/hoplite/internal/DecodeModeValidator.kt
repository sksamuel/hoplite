package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
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
    return if (result.unused.isEmpty()) result.valid() else {
      val errors = NonEmptyList.unsafe(result.unused.map { ConfigFailure.UnusedPath(it) })
      ConfigFailure.MultipleFailures(errors).invalid()
    }
  }
}

enum class DecodeMode {
  // errors if a config value is provided but not used
  Strict,

  // allows config to be unused
  Lenient
}
