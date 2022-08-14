package com.sksamuel.hoplite.secrets

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.Secret
import com.sksamuel.hoplite.StringNode
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * Implementations determine whether a node is considered a secret.
 */
interface SecretsPolicy {
  fun isSecret(node: Node, type: KType?): Boolean
}

/**
 * A [SecretsPolicy] that treats every field as if it is a secret.
 */
object EveryFieldSecretsPolicy : SecretsPolicy {
  override fun isSecret(node: Node, type: KType?): Boolean = true
}

/**
 * A [SecretsPolicy] that treats all string fields as secrets.
 */
object AllStringNodesSecretsPolicy : SecretsPolicy {
  override fun isSecret(node: Node, type: KType?): Boolean = node is StringNode
}

/**
 * A [SecretsPolicy] that treats all types marshalled to [Secret]s as a secret.
 */
object SecretTypeSecretsPolicy : SecretsPolicy {
  override fun isSecret(node: Node, type: KType?): Boolean = type == Secret::class.createType()
}

private val defaultSecretNames = setOf("secret", "credential", "pass")

/**
 * A [SecretsPolicy] that considers a config value a secret if the
 * key contains one of the given [names] (case-insensitive)
 */
class ByNameSecretsPolicy(private val names: Set<String> = defaultSecretNames) : SecretsPolicy {
  private val lowernames = names.map { it.lowercase() }
  override fun isSecret(node: Node, type: KType?): Boolean {
    val lower = node.path.flatten().lowercase()
    return lowernames.any { lower.contains(it) }
  }
}

/**
 * An [SecretsPolicy] that uses [CommonMetadata.IsSecretLookup] to determine if a value was
 * a remote secret.
 */
object MetadataSecretPolicy : SecretsPolicy {
  override fun isSecret(node: Node, type: KType?): Boolean {
    return node.meta[CommonMetadata.IsSecretLookup] == true
  }
}

class CompositeSecretsPolicy(private val policies: Set<SecretsPolicy>) : SecretsPolicy {
  override fun isSecret(node: Node, type: KType?): Boolean {
    return policies.any { it.isSecret(node, type) }
  }
}

/**
 * A [SecretsPolicy] which combines the [MetadataSecretPolicy], [ByNameSecretsPolicy] loaded
 * with [defaultSecretNames] and the [SecretTypeSecretsPolicy].
 */
val StandardSecretsPolicy = CompositeSecretsPolicy(
  setOf(
    MetadataSecretPolicy,
    ByNameSecretsPolicy(defaultSecretNames),
    SecretTypeSecretsPolicy,
  )
)
