package com.sksamuel.hoplite.azure

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.resolver.ContextResolver

class AzureKeyVaultContextResolver(
  private val report: Boolean = false,
  private val createClient: () -> SecretClient
) : ContextResolver() {

  constructor(url: String) : this(url, false)
  constructor(url: String, report: Boolean) : this(report = report, {
    SecretClientBuilder()
      .vaultUrl(url)
      .credential(DefaultAzureCredentialBuilder().build())
      .buildClient()
  })

  private val client = lazy { createClient() }
  private val ops = lazy { AzureOps(client.value) }

  override val contextKey = "azure-key-value"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return ops.value.fetchSecret(path, context, report)
  }
}
