package com.sksamuel.hoplite.aws2

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

/**
 * Fetches a secret from AWS Secrets manager, which can be added as a
 * map source for configuration
 */
class AwsSecretsManagerSource(
  private val json: Json = Json.Default,
  private val createClient: () -> SecretsManagerClient = { SecretsManagerClient.create() },
) {

  private val client by lazy { createClient() }

  fun fetchSecretAsMap(key: String): Map<String, String> {
    val secretRequest = GetSecretValueRequest.builder().secretId(key).build()
    val secret = client.getSecretValue(secretRequest)

    return json.decodeFromString<Map<String, String>>(secret.secretString())
  }

}
