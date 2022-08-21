package com.sksamuel.hoplite.azure

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
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

class GcpSecretManagerPreprocessor(private val createClient: () -> SecretManagerServiceClient) :
  TraversingPrimitivePreprocessor() {

  private val client = lazy { createClient() }

  private val regex = "gcpsm://(.+?)".toRegex()

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
      val resp = client.value.accessSecretVersion(AccessSecretVersionRequest.newBuilder().setName(key).build())
      val value = resp.payload.data.toStringUtf8()
      context.report("GCP Secret Manager Lookups", mapOf("Name" to key))
      if (value.isNullOrBlank())
        ConfigFailure.PreprocessorWarning("Empty value for '$key' in GCP Secret Manager").invalid()
      else
        node.copy(value = value)
          .withMeta(CommonMetadata.Secret, true)
          .withMeta(CommonMetadata.UnprocessedValue, node.value)
          .withMeta(CommonMetadata.RemoteLookup, "GCP '$key'")
          .valid()
    } catch (e: ApiException) {
      ConfigFailure.PreprocessorWarning("Could not locate secret '$key' in GCP Secret Manager").invalid()
    } catch (e: Exception) {
      ConfigFailure.PreprocessorFailure("Failed loading secret '$key' from GCP Secret Manager", e).invalid()
    }
  }
}
