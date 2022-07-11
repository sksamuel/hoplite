package com.sksamuel.hoplite.micrometer.datadog

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import io.micrometer.datadog.DatadogConfig
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.toJavaDuration

class DatadogConfigDecoder : Decoder<DatadogConfig> {

  override fun supports(type: KType): Boolean = type.classifier == DatadogConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<DatadogConfig> {
    return context.decoder(typeOf<InternalDatadogConfig>())
      .flatMap { it.decode(node, typeOf<InternalDatadogConfig>(), context) }
      .map { createConfig(it as InternalDatadogConfig) }
  }

  private fun createConfig(
    config: InternalDatadogConfig,
  ): DatadogConfig {
    return object : DatadogConfig {
      override fun apiKey(): String = config.apiKey
      override fun enabled(): Boolean = config.enabled
      override fun hostTag(): String? = config.hostTag
      override fun applicationKey(): String? = config.applicationKey
      override fun uri(): String = config.uri ?: "https://api.datadoghq.com"
      override fun descriptions(): Boolean = config.descriptions
      override fun step(): Duration = config.step.toJavaDuration()
      override fun batchSize(): Int = config.batchSize ?: 10000
      override fun get(key: String): String? = null
    }
  }
}

private data class InternalDatadogConfig(
  val apiKey: String,
  val hostTag: String?,
  val applicationKey: String?,
  val enabled: Boolean,
  val descriptions: Boolean,
  val uri: String?,
  val batchSize: Int?,
  val step: kotlin.time.Duration,
)
