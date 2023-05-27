package com.sksamuel.hoplite.aws

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.resolver.ContextResolver

abstract class AbstractAwsSecretsManagerContextResolver(
  private val report: Boolean = false,
  createClient: () -> AWSSecretsManager = { AWSSecretsManagerClientBuilder.standard().build() }
) : ContextResolver() {

  // should stay lazy so still be added to config even when not used, eg locally
  private val client by lazy { createClient() }
  private val ops by lazy { AwsOps(client) }

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    val (key, index) = ops.extractIndex(path)
    return ops.fetchSecret(key)
      .onSuccess { if (report) ops.report(context, it) }
      .flatMap { ops.parseSecret(it, index) }
  }
}
