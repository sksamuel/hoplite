package com.sksamuel.hoplite.micrometer.statsd

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import io.micrometer.statsd.StatsdConfig
import io.micrometer.statsd.StatsdFlavor
import io.micrometer.statsd.StatsdProtocol
import java.time.Duration
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class StatsdConfigDecoder : Decoder<StatsdConfig> {

  override fun supports(type: KType): Boolean = type.classifier == StatsdConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<StatsdConfig> {
    return context.decoder(typeOf<InternalConfig>())
      .flatMap { it.decode(node, typeOf<InternalConfig>(), context) }
      .map { createConfig(it as InternalConfig) }
  }

  private fun createConfig(
    config: InternalConfig
  ): StatsdConfig {
    return object : StatsdConfig {
      override fun buffered(): Boolean = config.buffered ?: super.buffered()
      override fun host(): String = config.host ?: super.host()
      override fun port(): Int = config.port ?: super.port()
      override fun enabled(): Boolean = config.enabled ?: super.enabled()
      override fun step(): Duration = config.step ?: super.step()
      override fun maxPacketLength(): Int = config.maxPacketLength ?: super.maxPacketLength()
      override fun pollingFrequency(): Duration = config.pollingFrequency ?: super.pollingFrequency()
      override fun publishUnchangedMeters(): Boolean = config.publishUnchangedMeters ?: super.publishUnchangedMeters()
      override fun flavor(): StatsdFlavor = config.flavor ?: super.flavor()
      override fun protocol(): StatsdProtocol = config.protocol ?: super.protocol()
      override fun get(key: String): String? = null
    }
  }
}

data class InternalConfig(
  val host: String?,
  val port: Int?,
  val enabled: Boolean?,
  val publishUnchangedMeters: Boolean?,
  val buffered: Boolean?,
  val maxPacketLength: Int?,
  val pollingFrequency: Duration?,
  val step: Duration?,
  val flavor: StatsdFlavor?,
  val protocol: StatsdProtocol?
)
