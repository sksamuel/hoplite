package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.sksamuel.hoplite.resolver.CompositeResolver
import com.sksamuel.hoplite.resolver.Resolver

fun createAwsSecretsManagerResolver(
  report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() },
): Resolver = CompositeResolver(AwsSecretsManagerContextResolver(report, createClient))
