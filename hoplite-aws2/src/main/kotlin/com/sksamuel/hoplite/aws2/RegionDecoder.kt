package com.sksamuel.hoplite.aws2

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.fp.invalid
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
        // Use Locale.ROOT for the case fold. AWS region IDs are ASCII identifiers
        // (e.g. "us-east-1") that must compare verbatim against `Region.id()`. Using
        // Locale.getDefault() means a Turkish-locale machine folds "I" to dotless "ı"
        // instead of "i" — region names happen not to contain "I" today, but this is
        // exactly the class of latent bug that bites the day someone does add an I to
        // an AWS region. Pin to the locale-invariant lower-case algorithm.
        Region.regions().single { it.id() == name.lowercase(Locale.ROOT).replace("_", "-") }
      }.toValidated { ConfigFailure.Generic("Cannot create region from $name") }

    return when (node) {
      is StringNode -> regionFromName(node.value)
      else -> ConfigFailure.DecodeError(node, type).invalid()
    }
  }
}
