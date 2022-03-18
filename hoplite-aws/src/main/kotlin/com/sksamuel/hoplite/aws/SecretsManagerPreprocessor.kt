package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

class AwsSecretsManagerPreprocessor(
  private val createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex1 = "\\$\\{awssecret:(.+?)}".toRegex()

  private fun fetchValue(key: String): Result<String> = runCatching {
    val req = GetSecretValueRequest().withSecretId(key)
    client.getSecretValue(req).secretString
  }

  override fun handle(node: PrimitiveNode): Node = when (node) {
    is StringNode -> {
      when (val match = regex1.matchEntire(node.value)) {
        null -> node
        else -> {
          val key = match.groupValues[1]
          val value = fetchValue(key)
            .getOrElse { throw ConfigException("Failed loading secrets value from key '$key'", it) }
          node.copy(value = value)
        }
      }
    }
    else -> node
  }
}
