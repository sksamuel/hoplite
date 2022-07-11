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

class DatadogConfigDecoder : Decoder<DatadogConfig> {

  override fun supports(type: KType): Boolean = type.classifier == DatadogConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<DatadogConfig> {
    return context.decoder(typeOf<InternalConfig>())
      .flatMap { it.decode(node, typeOf<InternalConfig>(), context) }
      .map { createConfig(it as InternalConfig) }
  }

  private fun createConfig(
    config: InternalConfig,
  ): DatadogConfig {
    return object : DatadogConfig {
      override fun apiKey(): String = config.apiKey
      override fun enabled(): Boolean = config.enabled ?: true
      override fun hostTag(): String? = config.hostTag
      override fun applicationKey(): String? = config.applicationKey
      override fun uri(): String = config.uri ?: "https://api.datadoghq.com"
      override fun descriptions(): Boolean = config.descriptions ?: true
      override fun step(): Duration = config.step ?: Duration.ofMinutes(1)
      override fun batchSize(): Int = config.batchSize ?: 10000
      override fun get(key: String): String? = null
    }
  }
}

data class InternalConfig(
  val apiKey: String,
  val hostTag: String?,
  val applicationKey: String?,
  val enabled: Boolean?,
  val descriptions: Boolean?,
  val uri: String?,
  val batchSize: Int?,
  val step: Duration?,
)
