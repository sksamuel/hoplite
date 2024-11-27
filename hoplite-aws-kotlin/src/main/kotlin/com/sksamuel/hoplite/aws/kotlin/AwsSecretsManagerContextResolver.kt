package com.sksamuel.hoplite.aws.kotlin

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import kotlinx.coroutines.runBlocking

/**
 * Replaces strings of the form ${{ aws-secrets-manager:path }} by looking up the path in AWS Secrets Manager.
 *
 * The [SecretsManagerClient] client is created from the [createClient] argument which uses the
 * standard [SecretsManagerClient.fromEnvironment()] by default.
 */
class AwsSecretsManagerContextResolver(
  report: Boolean = false,
  createClient: () -> SecretsManagerClient = { runBlocking { SecretsManagerClient.fromEnvironment() } }
) : AbstractAwsSecretsManagerContextResolver(report, createClient) {
  override val contextKey: String = "aws-secrets-manager"
  override val default: Boolean = false
}
