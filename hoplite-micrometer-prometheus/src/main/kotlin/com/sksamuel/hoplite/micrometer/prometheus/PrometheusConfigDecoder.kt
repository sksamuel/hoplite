package com.sksamuel.hoplite.micrometer.prometheus

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import io.micrometer.prometheus.PrometheusConfig
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.toJavaDuration

class PrometheusConfigDecoder : Decoder<PrometheusConfig> {

  override fun supports(type: KType): Boolean = type.classifier == PrometheusConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<PrometheusConfig> {
    return context.decoder(typeOf<InternalConfig>())
      .flatMap { it.decode(node, typeOf<InternalConfig>(), context) }
      .map { createConfig(it as InternalConfig) }
  }

  private fun createConfig(
    config: InternalConfig,
  ): PrometheusConfig {
    return object : PrometheusConfig {
      override fun step(): Duration = config.step?.toJavaDuration() ?: Duration.ofMinutes(1)
      override fun descriptions(): Boolean = config.descriptions ?: true
      override fun get(key: String): String? = null
    }
  }
}

data class InternalConfig(
  val descriptions: Boolean?,
  val step: kotlin.time.Duration?,
)
