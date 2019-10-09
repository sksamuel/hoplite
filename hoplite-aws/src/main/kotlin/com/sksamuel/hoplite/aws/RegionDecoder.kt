package com.sksamuel.hoplite.aws

import arrow.core.Try
import arrow.data.invalid
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.toValidated
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.NonNullableDecoder
import kotlin.reflect.KType

class RegionDecoder : NonNullableDecoder<Region> {

  override fun supports(type: KType): Boolean = type.classifier == Region::class

  override fun safeDecode(node: Value,
                          type: KType,
                          registry: DecoderRegistry): ConfigResult<Region> {
    fun regionFromName(name: String): ConfigResult<Region> =
        Try { Region.getRegion(Regions.fromName(name)) }
          .toValidated { ConfigFailure.Generic("Cannot create region from $name") }

    return when (node) {
      is StringValue -> regionFromName(node.value)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
