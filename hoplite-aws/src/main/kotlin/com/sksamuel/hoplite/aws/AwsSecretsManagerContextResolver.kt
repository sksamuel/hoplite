package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder

/**
 * Replaces strings of the form ${{ aws-secrets-manager:path }} by looking up the path in AWS Secrets Manager.
 * The [AWSSecretsManager] client is created from the [createClient] function which by default
 * uses the default builder.
 */
class AwsSecretsManagerContextResolver(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : AbstractAwsSecretsManagerContextResolver(report, createClient) {
  override val contextKey: String = "aws-secrets-manager"
  override val default: Boolean = false
}

@Deprecated("Included for backwards compatibility")
class Legacy1AwsSecretsManagerContextResolver(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : AbstractAwsSecretsManagerContextResolver(report, createClient) {
  override val contextKey: String = "awssm"
  override val default: Boolean = false
}

@Deprecated("Included for backwards compatibility")
class Legacy2AwsSecretsManagerContextResolver(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : AbstractAwsSecretsManagerContextResolver(report, createClient) {
  override val contextKey: String = "secretsmanager"
  override val default: Boolean = false
}

@Deprecated("Included for backwards compatibility")
class Legacy3AwsSecretsManagerContextResolver(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : AbstractAwsSecretsManagerContextResolver(report, createClient) {
  override val contextKey: String = "awssecret"
  override val default: Boolean = false
}
