package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.transform
import kotlin.reflect.KType

/**
 * A decoder which decodes based on unnormalized keys.
 *
 * This is useful for decoders that need to know the original key names.
 *
 * It restores the original key names from the node source key.
 */
abstract class AbstractUnnormalizedKeysDecoder<T> : NullHandlingDecoder<T> {
  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> {
    val unnormalizedNode = node.transform {
      val sourceKey = it.sourceKey
      when (it) {
        is MapNode -> it.copy(map = it.map.mapKeys { (k, v) ->
          (v.sourceKey ?: k).removePrefix("$sourceKey.")
        })
        else -> it
      }
    }

    return safeDecodeUnnormalized(unnormalizedNode, type, context)
  }

  abstract fun safeDecodeUnnormalized(node: Node, type: KType, context: DecoderContext): ConfigResult<T>
}
