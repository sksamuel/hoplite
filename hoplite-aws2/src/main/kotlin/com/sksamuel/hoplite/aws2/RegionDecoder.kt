package com.sksamuel.hoplite.aws2

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.result.toValidated
import software.amazon.awssdk.regions.Region
import java.util.Locale
import kotlin.reflect.KType

class RegionDecoder : NullHandlingDecoder<Region> {

  override fun supports(type: KType): Boolean = type.classifier == Region::class

  override fun safeDecode(
    node: Node,
    type: KType,
    context: DecoderContext
  ): ConfigResult<Region> {
    fun regionFromName(name: String): ConfigResult<Region> =
      runCatching {
        Region.regions().single { it.id() == name.lowercase(Locale.getDefault()).replace("_", "-") }
      }.toValidated { ConfigFailure.Generic("Cannot create region from $name") }

    return when (node) {
      is StringNode -> regionFromName(node.value)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
