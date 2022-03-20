package com.sksamuel.hoplite.consul

import com.orbitz.consul.Consul
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import java.util.Optional

/**
 * Creates a Preprocessor that will replace strings of the form `${consul:a.b.c}` with the
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

  private fun fetchConsulValue(key: String): Result<Optional<String>> = runCatching {
    client.keyValueClient().getValueAsString(key)
  }

  override fun handle(node: PrimitiveNode): ConfigResult<Node> = when (node) {
    is StringNode -> when (val match = regex.matchEntire(node.value)) {
      null -> node.valid()
      else -> {
        val key = match.groupValues[1]
        fetchConsulValue(key).fold(
          {
            when (val v = it.orElseGet { null }) {
              null -> ConfigFailure.PreprocessorWarning("Unable to locate consul key '$key'").invalid()
              else -> node.copy(value = v).valid()
            }
          },
          { ConfigFailure.PreprocessorFailure("Failed loading from consul", it).invalid() }
        )
      }
    }
    else -> node.valid()
  }
}
