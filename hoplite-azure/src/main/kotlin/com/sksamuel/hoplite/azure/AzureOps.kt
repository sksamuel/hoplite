package com.sksamuel.hoplite.azure

import com.azure.core.exception.ResourceNotFoundException
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid

class AzureOps(private val client: SecretClient) {

  fun report(context: DecoderContext, secret: KeyVaultSecret) {
    context.report(
      Section,
      mapOf(
        "Key" to secret.name,
        "Created" to secret.properties.createdOn,
        "Version" to secret.properties.version,
        "Key ID" to secret.properties.keyId
      )
    )
  }

  fun fetchSecret(key: String, context: DecoderContext, report: Boolean): ConfigResult<String?> {
    return try {

      val secret: KeyVaultSecret = client.getSecret(key)
      val value: String? = secret.value

      if (report) report(context, secret)

      if (value.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty value for '$key' in Azure Key Vault").invalid()
      else
        value.valid()
    } catch (e: ResourceNotFoundException) {
      ConfigFailure.PreprocessorWarning("Could not locate resource '$key' in Azure Key Vault").invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from Azure Key Vault", e).invalid()
    }
  }

  companion object {
    const val Section = "Azure KeyVault Lookups"
  }
}
