package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.decoder.DotPath

/**
 * Implementations determine which fields are secrets.
 */
interface Secrets {
  fun isSecret(path: DotPath, secrets: Set<DotPath>): Boolean
}

object DefaultSecrets : Secrets {
  override fun isSecret(path: DotPath, secrets: Set<DotPath>): Boolean {
    val lower = path.flatten().lowercase()
    return lower.contains("password") ||
      lower.contains("secret") ||
      secrets.any { it.flatten().lowercase().contains(lower) }
  }
}
