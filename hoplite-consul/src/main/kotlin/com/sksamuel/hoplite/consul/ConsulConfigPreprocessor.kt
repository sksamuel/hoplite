package com.sksamuel.hoplite.consul

import com.orbitz.consul.Consul
import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.withMeta
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
  private val report: Boolean = false,
  private val configure: (Consul.Builder) -> Unit = {}
) : TraversingPrimitivePreprocessor() {

  private val client by lazy { createClient() }
  private val regex1 = "\\$\\{consul:(.+?)}".toRegex()
  private val regex2 = "consul://(.+?)".toRegex()

  private fun createClient(): Consul {
    val builder = Consul.builder().withUrl(url)
    configure.invoke(builder)
    return builder.build()
  }

  private fun fetchConsulValue(key: String): Result<Optional<String>> = runCatching {
    client.keyValueClient().getValueAsString(key)
  }

  override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
    is StringNode -> when (val match = regex1.matchEntire(node.value) ?: regex2.matchEntire(node.value)) {
      null -> node.valid()
      else -> {
        val key = match.groupValues[1]
        fetchConsulValue(key).fold(
          {
            when (val v = it.orElseGet { null }) {
              null -> ConfigFailure.PreprocessorWarning("Unable to locate consul key '$key'").invalid()
              else -> {
                if (report)
                  context.report("Consul Lookups", mapOf("Key" to key))
                node.copy(value = v).withMeta(CommonMetadata.UnprocessedValue, node.value).valid()
              }
            }
          },
          { ConfigFailure.PreprocessorFailure("Failed loading from consul", it).invalid() }
        )
      }
    }
    else -> node.valid()
  }
}
