package com.sksamuel.hoplite.micrometer.datadog

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
import io.micrometer.datadog.DatadogConfig
import kotlin.reflect.KType

class DatadogConfigDecoder : Decoder<DatadogConfig> {

  override fun supports(type: KType): Boolean = type.classifier == DatadogConfig::class

  override fun decode(
    node: Node,
    type: KType,
    context: DecoderContext,
  ): ConfigResult<DatadogConfig> {
    return when (node) {
      is MapNode -> createDatadogConfig(node)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }

  private fun createDatadogConfig(node: MapNode): Validated<ConfigFailure, DatadogConfig> {
    return object : DatadogConfig {
      override fun get(key: String): String? = node[key.removePrefix(prefix() + ".")].valueOrNull()
    }.valid()
  }
}
