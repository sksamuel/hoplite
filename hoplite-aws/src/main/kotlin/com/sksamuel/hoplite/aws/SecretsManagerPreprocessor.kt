package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

class AwsSecretsManagerPreprocessor(
  private val createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex1 = "\\$\\{awssecret:(.+?)}".toRegex()
  private val regex2 = "secretsmanager://(.+?)".toRegex()

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> {
      when (val match = regex1.matchEntire(node.value)
        ?: regex2.matchEntire(node.value)
      ) {
        null -> node.valid()
        else -> {
          val key = match.groupValues[1]
          try {
            val req = GetSecretValueRequest().withSecretId(key)
            val value = client.getSecretValue(req).secretString
            node.copy(value = value).valid()
          } catch (e: ResourceNotFoundException) {
            ConfigFailure.PreprocessorWarning("Could not locate secret '$key' in AWS SecretsManager").invalid()
          } catch (e: Exception) {
            ConfigFailure.PreprocessorFailure("Failed loading secrets from AWS SecretsManager", e).invalid()
          }
        }
      }
    }
    else -> node.valid()
  }
}
