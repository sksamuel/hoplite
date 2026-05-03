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

    // GetSecretValueResponse.secretString() returns null when the secret was created with
    // SecretBinary instead of a string payload. Without this guard the next line would NPE
    // on a Java platform-type result. Surface a clearer message so the caller knows the
    // secret was binary and can switch to fetching the binary form.
    val secretString = secret.secretString()
      ?: error("AWS secret '$key' has no string value (was it created with SecretBinary?)")

    return json.decodeFromString<Map<String, String>>(secretString)
  }

}
