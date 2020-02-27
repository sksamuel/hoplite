package com.sksamuel.hoplite.aws

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.Try
import com.sksamuel.hoplite.fp.invalid
import kotlin.reflect.KType

class RegionDecoder : NullHandlingDecoder<Region> {

  override fun supports(type: KType): Boolean = type.classifier == Region::class

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Region> {
    fun regionFromName(name: String): ConfigResult<Region> =
        Try { Region.getRegion(Regions.fromName(name)) }
          .toValidated { ConfigFailure.Generic("Cannot create region from $name") }

    return when (node) {
      is StringNode -> regionFromName(node.value)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
