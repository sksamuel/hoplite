package com.sksamuel.hoplite.vault

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.withMeta
import org.springframework.vault.core.VaultKeyValueOperationsSupport
import org.springframework.vault.core.VaultTemplate

class VaultSecretPreprocessor(
  private val createClient: () -> VaultTemplate,
  private val apiVersion: VaultKeyValueOperationsSupport.KeyValueBackend = VaultKeyValueOperationsSupport.KeyValueBackend.KV_2,
) : TraversingPrimitivePreprocessor() {

  constructor(
    client: VaultTemplate,
    apiVersion: VaultKeyValueOperationsSupport.KeyValueBackend = VaultKeyValueOperationsSupport.KeyValueBackend.KV_2,
  ) : this(
    { client },
    apiVersion
  )

  private val client by lazy { createClient() }
  private val regex = "vault://(.+?)".toRegex()
  private val tokenRegex = "(.+)\\s+(.+)".toRegex()

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
        null -> node.valid()
        else -> {
          when (val match2 = tokenRegex.matchEntire(match.groupValues[1])) {
            null -> ConfigFailure.PreprocessorWarning("Must specify vault key at '${match.groupValues[0]}'").invalid()
            else -> {
              fetchSecret(match2.groupValues[1], match2.groupValues[2], node)
            }
          }
        }
      }
    }
    else -> node.valid()
  }

  private fun fetchSecret(path: String, key: String, node: StringNode): ConfigResult<Node> = runCatching {

    val paths = path.split("/")
    if (paths.size < 2) return ConfigFailure.PreprocessorWarning("Invalid vault path '$path'").invalid()

    val ops = client.opsForKeyValue(paths[0], apiVersion)
    val pathSecret = ops.get(paths.drop(1).joinToString("/"))
      ?: return ConfigFailure.PreprocessorWarning("Vault path '$path' not found").invalid()

    val data = pathSecret.data
    if (data == null) {
      ConfigFailure.PreprocessorWarning("No data at path '$path' in Vault").invalid()
    } else {
      val value = data[key]
      if (value == null) {
        ConfigFailure.PreprocessorWarning("Vault key '$key' not found in path '$path'").invalid()
      } else {
        node.copy(value = value.toString())
          .withMeta(CommonMetadata.IsSecretLookup, true)
          .withMeta(CommonMetadata.UnprocessedValue, node.value)
          .withMeta(CommonMetadata.RemoteLookup, "Vault '$path' '$key'")
          .valid()
      }
    }
  }.getOrElse {
    ConfigFailure.PreprocessorFailure("Failed loading secret '$path $key' from Vault", it).invalid()
  }
}
