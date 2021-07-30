package com.sksamuel.hoplite.consul

import com.orbitz.consul.Consul
import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor

/**
 * Creates a [Preprocessor] that will replace strings of the form `${consul:a.b.c}` with the
 * configuration value specified by `a.b.c` from a consul service.
 *
 * @param url the url of the consul config service.
 * @param configure optional configuration of the consul client
 */
class ConsulConfigPreprocessor(
  private val url: String,
  private val configure: (Consul.Builder) -> Unit = {},
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex = "\\$\\{consul:(.+?)}".toRegex()

  private fun createClient(): Consul {
    val builder = Consul.builder().withUrl(url)
    configure.invoke(builder)
    return builder.build()
  }

  private fun fetchConsulValue(key: String): Try<String> = Try {
    client.keyValueClient().getValueAsString(key).orElseThrow()
  }

  override fun handle(node: PrimitiveNode): Node = when (node) {
    is StringNode -> {
      when (val match = regex.matchEntire(node.value)) {
        null -> node
        else -> {
          val key = match.groupValues[1]
          val value = fetchConsulValue(key)
            .getOrElse { throw ConfigException("Failed loading consul config '$key'", it) }
          node.copy(value = value)
        }
      }
    }
    else -> node
  }
}

data class BasicAuth(val user: String, val password: String)
