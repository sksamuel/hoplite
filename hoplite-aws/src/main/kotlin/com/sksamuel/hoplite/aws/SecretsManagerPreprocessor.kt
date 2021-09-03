package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

class AwsSecretsManagerPreprocessor(
  private val configure: (AWSSecretsManagerClientBuilder) -> Unit = {}
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex = "\\$\\{awssecret:(.+?)}".toRegex()

  private fun createClient(): AWSSecretsManager {
    val builder = AWSSecretsManagerClientBuilder.standard()
    configure.invoke(builder)
    return builder.build()
  }

  private fun fetchValue(key: String): Try<String> = Try {
    val req = GetSecretValueRequest().withSecretId(key)
    client.getSecretValue(req).secretString
  }

  override fun handle(node: PrimitiveNode): Node = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
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
