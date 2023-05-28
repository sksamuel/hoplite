package com.sksamuel.hoplite.gcp

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient

class GcpSecretManagerContextResolver(
  report: Boolean = false,
  createClient: () -> SecretManagerServiceClient,
) : AbstractGcpSecretManagerContextResolver(report, createClient) {
  override val contextKey: String = "gcp-secrets-manager"
  override val default: Boolean = false
}
