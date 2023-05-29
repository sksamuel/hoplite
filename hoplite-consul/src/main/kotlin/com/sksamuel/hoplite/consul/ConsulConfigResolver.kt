package com.sksamuel.hoplite.consul

import com.orbitz.consul.Consul
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.resolver.context.ContextResolver
import java.util.Optional

/**
 * Creates a [ContextResolver] that will replace strings of the form `${{ consul:a.b.c }}` with the
 * configuration value specified by `a.b.c` from a consul service.
 *
 * @param url the url of the consul config service.
 * @param configure optional configuration of the consul client
 */
class ConsulConfigResolver(
  private val url: String,
  private val report: Boolean = false,
  private val configure: (Consul.Builder) -> Unit = {}
) : ContextResolver() {

  private val client by lazy { createClient() }

  override val contextKey: String = "consul"
  override val default: Boolean = false

  private fun createClient(): Consul {
    val builder = Consul.builder().withUrl(url)
    configure.invoke(builder)
    return builder.build()
  }

  private fun fetchConsulValue(key: String): Result<Optional<String>> = runCatching {
    client.keyValueClient().getValueAsString(key)
  }

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return fetchConsulValue(path).fold(
      {
        when (val v = it.orElseGet { null }) {
          null -> ConfigFailure.PreprocessorWarning("Unable to locate consul key '$path'").invalid()
          else -> {
            if (report)
              context.report("Consul Lookups", mapOf("Key" to path))
            v.valid()
          }
        }
      },
      { ConfigFailure.PreprocessorFailure("Failed loading from consul", it).invalid() }
    )
  }
}
