package com.sksamuel.hoplite.resolver.validator

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.valueOrNull

class PortValidator : Resolver {
  override suspend fun resolve(
    paramName: String?,
    node: Node,
    root: Node,
    context: DecoderContext
  ): ConfigResult<Node> {
    return if (paramName == "port") {
      val port = node.valueOrNull()?.toLongOrNull() ?: 0L
      return if (port > 0 && port < Short.MAX_VALUE) node.valid() else
        ConfigFailure.ValidationFailed("Invalid port: $port", node).invalid()
    } else node.valid()
  }
}
