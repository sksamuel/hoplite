package com.sksamuel.hoplite.aws

import arrow.core.Try
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigResults
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.toValidated
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.NonNullableDecoder
import kotlin.reflect.KType

class RegionDecoder : NonNullableDecoder<Region> {

  override fun supports(type: KType): Boolean = type.classifier == Region::class

  override fun safeDecode(node: Node, type: KType, registry: DecoderRegistry, path: String): ConfigResult<Region> {
    fun regionFromName(name: String): ConfigResult<Region> =
        Try { Region.getRegion(Regions.fromName(name)) }
            .toValidated { ConfigFailure("Cannot create region from $name") }
            .toValidatedNel()

    return when (node) {
      is StringNode -> regionFromName(node.value)
      else -> ConfigResults.decodeFailure(node, path, Region::class)
    }
  }
}
