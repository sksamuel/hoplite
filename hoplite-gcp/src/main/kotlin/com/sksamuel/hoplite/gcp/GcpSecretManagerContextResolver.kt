package com.sksamuel.hoplite.gcp

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.sksamuel.hoplite.resolver.CompositeResolver
import com.sksamuel.hoplite.resolver.Resolver

class GcpSecretManagerContextResolver(
  report: Boolean = false,
  createClient: () -> SecretManagerServiceClient,
) : AbstractGcpSecretManagerContextResolver(report, createClient) {
  override val contextKey: String = "gcp-secrets-manager"
  override val default: Boolean = false
}

@Deprecated("Included for backwards compatibility. Prefer GcpSecretManagerContextResolver")
class LegacyGcpSecretManagerContextResolver(
  report: Boolean = false,
  createClient: () -> SecretManagerServiceClient,
) : AbstractGcpSecretManagerContextResolver(report, createClient) {
  override val contextKey: String = "gcpsm"
  override val default: Boolean = false
}

fun GcpSecretsManagerContextResolvers(
  report: Boolean = false,
  createClient: () -> SecretManagerServiceClient,
): Resolver = CompositeResolver(
  GcpSecretManagerContextResolver(report, createClient),
  LegacyGcpSecretManagerContextResolver(report, createClient),
)
