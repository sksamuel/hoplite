package com.sksamuel.hoplite.azure

import com.azure.core.exception.ResourceNotFoundException
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.withMeta

class AzureKeyVaultPreprocessor(
  private val report: Boolean = false,
  private val createClient: () -> SecretClient,
) : TraversingPrimitivePreprocessor() {

  constructor(url: String) : this(url, false)
  constructor(url: String, report: Boolean) : this(report = report, {
    SecretClientBuilder()
      .vaultUrl(url)
      .credential(DefaultAzureCredentialBuilder().build())
      .buildClient();
  })

  private val client = lazy { createClient() }

  private val regex = "azurekeyvault://(.+?)".toRegex()

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
        null -> node.valid()
        else -> fetchSecret(match.groupValues[1].trim(), node, context)
      }
    }
    else -> node.valid()
  }

  private fun fetchSecret(key: String, node: StringNode, context: DecoderContext): ConfigResult<Node> {
    return try {
      val secret = client.value.getSecret(key)
      val value = secret.value

      if (report)
        context.report(
          "Azure KeyVault Lookups",
          mapOf(
            "Key" to key,
            "Created" to secret.properties.createdOn,
            "Version" to secret.properties.version,
            "Key ID" to secret.properties.keyId
          )
        )

      if (value.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty value for '$key' in Azure Key Vault").invalid()
      else
        node.copy(value = value)
          .withMeta(CommonMetadata.Secret, true)
          .withMeta(CommonMetadata.UnprocessedValue, node.value)
          .withMeta(CommonMetadata.RemoteLookup, "Azure '$key'")
          .valid()
    } catch (e: ResourceNotFoundException) {
      ConfigFailure.PreprocessorWarning("Could not locate resource '$key' in Azure Key Vault").invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from Azure Key Vault", e).invalid()
    }
  }
}
