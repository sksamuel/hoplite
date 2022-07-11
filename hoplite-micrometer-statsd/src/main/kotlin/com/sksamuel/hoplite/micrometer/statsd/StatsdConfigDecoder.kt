package com.sksamuel.hoplite.micrometer.statsd

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import io.micrometer.statsd.StatsdConfig
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.toJavaDuration

class StatsdConfigDecoder : Decoder<StatsdConfig> {

  override fun supports(type: KType): Boolean = type.classifier == StatsdConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<StatsdConfig> {
    return context.decoder(typeOf<InternalConfig>())
      .flatMap { it.decode(node, typeOf<InternalConfig>(), context) }
      .map { createConfig(it as InternalConfig) }
  }

  private fun createConfig(
    config: InternalConfig,
  ): StatsdConfig {
    return object : StatsdConfig {
      override fun buffered(): Boolean = config.buffered ?: true
      override fun host(): String = config.host ?: "localhost"
      override fun port(): Int = config.port ?: 8125
      override fun enabled(): Boolean = config.enabled ?: true
      override fun step(): Duration = config.step?.toJavaDuration() ?: Duration.ofMinutes(1)
      override fun maxPacketLength(): Int = config.maxPacketLength ?: 1400
      override fun pollingFrequency(): Duration = config.pollingFrequency?.toJavaDuration() ?: Duration.ofSeconds(10)
      override fun publishUnchangedMeters(): Boolean = config.publishUnchangedMeters ?: true
      override fun get(key: String): String? = null
    }
  }
}

private data class InternalConfig(
  val host: String?,
  val port: Int?,
  val enabled: Boolean?,
  val publishUnchangedMeters: Boolean?,
  val buffered: Boolean?,
  val maxPacketLength: Int?,
  val pollingFrequency: kotlin.time.Duration?,
  val step: kotlin.time.Duration?,
)
