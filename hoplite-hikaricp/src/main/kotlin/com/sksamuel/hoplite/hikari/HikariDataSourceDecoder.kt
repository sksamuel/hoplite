package com.sksamuel.hoplite.hikari

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.decoder.AbstractUnnormalizedKeysDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.util.Properties
import kotlin.reflect.KType

class HikariDataSourceDecoder : AbstractUnnormalizedKeysDecoder<HikariDataSource>() {

  override fun supports(type: KType): Boolean = type.classifier == HikariDataSource::class

  override fun safeDecodeUnnormalized(node: Node, type: KType, context: DecoderContext): ConfigResult<HikariDataSource> {

    val props = Properties()

    fun populate(node: Node, prefix: String) {
      when (node) {
        is MapNode -> node.map.forEach { (k, v) -> populate(v, if (prefix == "") k else "$prefix.$k") }
        is PrimitiveNode -> props[prefix] = node.value
        else -> {
        }
      }
    }

    return when (node) {
      is MapNode -> {
        populate(node, "")
        val config = HikariConfig(props)
        val ds = HikariDataSource(config)
        return ds.valid()
      }
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
