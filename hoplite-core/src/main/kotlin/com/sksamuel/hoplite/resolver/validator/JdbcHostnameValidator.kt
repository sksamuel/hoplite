package com.sksamuel.hoplite.resolver.validator

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.Resolver
import java.net.InetAddress
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class JdbcHostnameValidator(private val timeout: Duration = 5.seconds) : Resolver {

  override suspend fun resolve(
    paramName: String?,
    node: Node,
    root: Node,
    context: DecoderContext
  ): ConfigResult<Node> {
    return if (node is StringNode) {
      if (node.value.startsWith("jdbc:")) {
        val url = URI.create(node.value.removePrefix("jdbc:"))
        val hostname = url.host
        val failure = ConfigFailure.ValidationFailed("JDBC hostname `$hostname` not reachable", node).invalid()
        runCatching {
          val reachable = InetAddress.getByName(hostname).isReachable(timeout.inWholeMilliseconds.toInt())
          if (reachable) node.valid() else failure
        }.getOrElse { failure }
      } else node.valid()
    } else {
      node.valid()
    }
  }
}
