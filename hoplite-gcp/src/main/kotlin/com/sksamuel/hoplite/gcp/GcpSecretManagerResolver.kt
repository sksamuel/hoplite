package com.sksamuel.hoplite.gcp

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.ContextResolver

class GcpSecretManagerResolver(
  private val report: Boolean = false,
  private val createClient: () -> SecretManagerServiceClient,
) : ContextResolver() {

  private val client = lazy { createClient() }
  override val contextKey: String = "gcp-secrets-manager"
  override val default: Boolean = false

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return fetchSecret(path, context)
  }

  private fun fetchSecret(key: String, context: DecoderContext): ConfigResult<String?> {
    return try {
      val resp = client.value.accessSecretVersion(AccessSecretVersionRequest.newBuilder().setName(key).build())
      val value = resp.payload.data.toStringUtf8()

      if (report) {
        context.report(Section, mapOf("Key" to key))
      }

      if (value.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty value for '$key' in GCP Secret Manager").invalid()
      else
        value.valid()
    } catch (e: ApiException) {
      ConfigFailure.PreprocessorWarning("Could not locate secret '$key' in GCP Secret Manager").invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from GCP Secret Manager", e).invalid()
    }
  }

  companion object {
    const val Section = "GCP Secret Manager Lookups"
  }
}
