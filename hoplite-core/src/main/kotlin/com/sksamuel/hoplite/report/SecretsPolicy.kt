package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.decoder.DotPath

/**
 * Implementations determine whether a config value should be obfuscated in reports.
 */
interface SecretsPolicy {
  fun isSecret(path: DotPath, secretTypes: Set<DotPath>): Boolean
}

/**
 * A [SecretsPolicy] that treats every field as if it is a secret.
 */
object EveryFieldSecretsPolicy : SecretsPolicy {
  override fun isSecret(path: DotPath, secretTypes: Set<DotPath>): Boolean = true
}

/**
 * A [SecretsPolicy] that considers a config value a secret only if it was marshalled
 * to a [com.sksamuel.hoplite.Secret] type.
 */
object ByTypeSecretPolicy : SecretsPolicy {
  override fun isSecret(path: DotPath, secretTypes: Set<DotPath>): Boolean = secretTypes.contains(path)
}

/**
 * A [SecretsPolicy] that considers a config value a secret if the path contains one of the given [names].
 */
class ByNameSecretPolicy(private val names: Set<String>) : SecretsPolicy {
  private val lowernames = names.map { it.lowercase() }
  override fun isSecret(path: DotPath, secretTypes: Set<DotPath>): Boolean {
    val lower = path.flatten().lowercase()
    return lowernames.any { it.contains(lower) }
  }
}
