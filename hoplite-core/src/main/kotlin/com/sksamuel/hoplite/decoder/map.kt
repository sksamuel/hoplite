package com.sksamuel.hoplite.decoder

import arrow.core.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class MapDecoder : NullHandlingDecoder<Map<*, *>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(Map::class.starProjectedType) ||
      type.isSubtypeOf(Map::class.starProjectedType.withNullability(true))

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<Map<*, *>> {
    require(type.arguments.size == 2)

    val kType = type.arguments[0].type!!
    val vType = type.arguments[1].type!!

    fun <K, V> decode(node: MapNode,
                      kdecoder: Decoder<K>,
                      vdecoder: Decoder<V>,
                      context: DecoderContext): ConfigResult<Map<*, *>> {

      return node.map.entries.map { (k, v) ->
        kdecoder.decode(StringNode(k, node.pos), kType, context).flatMap { kk ->
          vdecoder.decode(v, vType, context).map { vv ->
            kk to vv
          }
        }
      }.sequence()
        .leftMap { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toMap() }
    }

    return context.decoder(kType).flatMap { kdecoder ->
      context.decoder(vType).flatMap { vdecoder ->
        when (node) {
          is MapNode -> decode(node, kdecoder, vdecoder, context)
          else -> ConfigFailure.UnsupportedCollectionType(node, "Map").invalid()
        }
      }
    }
  }
}
