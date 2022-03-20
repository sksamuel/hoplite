package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.decoder.DotPath

/**
 * Implementations determine which fields are secrets.
 */
interface SecretsPolicy {
  fun isSecret(path: DotPath, secrets: Set<DotPath>): Boolean
}

/**
 * A [SecretsPolicy] which specifies that all fields should be treated as secrets.
 */
object AllFieldsSecretsPolicy : SecretsPolicy {
  override fun isSecret(path: DotPath, secrets: Set<DotPath>): Boolean = true
}

object DefaultSecretsPolicy : SecretsPolicy {
  override fun isSecret(path: DotPath, secrets: Set<DotPath>): Boolean {
    val lower = path.flatten().lowercase()
    return lower.contains("password") ||
      lower.contains("secret") ||
      secrets.any { it.flatten().lowercase().contains(lower) }
  }
}
