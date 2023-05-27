package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.sksamuel.hoplite.resolver.CompositeResolver
import com.sksamuel.hoplite.resolver.Resolver

fun AwsSecretsManagerContextResolvers(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
): Resolver = CompositeResolver(
  AwsSecretsManagerContextResolver(report, createClient),
  Legacy1AwsSecretsManagerContextResolver(report, createClient),
  Legacy2AwsSecretsManagerContextResolver(report, createClient),
  Legacy3AwsSecretsManagerContextResolver(report, createClient),
)
