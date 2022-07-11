package com.sksamuel.hoplite.micrometer.prometheus

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.valueOrNull
import io.micrometer.prometheus.PrometheusConfig
import kotlin.reflect.KType

class PrometheusConfigDecoder : Decoder<PrometheusConfig> {

  override fun supports(type: KType): Boolean = type.classifier == PrometheusConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<PrometheusConfig> {
    return when (node) {
      is MapNode -> createConfig(node, context)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }

  private fun createConfig(
    node: MapNode,
    context: DecoderContext
  ): Validated<ConfigFailure, PrometheusConfig> {
    return object : PrometheusConfig {
      override fun get(key: String): String? {
        val k = key.removePrefix(prefix() + ".")
        val value = node[k].valueOrNull()
        context.usedPaths.add(node.atKey(k).path)
        return value
      }
    }.valid()
  }
}
