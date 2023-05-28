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
