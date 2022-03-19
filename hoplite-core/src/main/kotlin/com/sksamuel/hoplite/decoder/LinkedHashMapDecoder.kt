package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class LinkedHashMapDecoder : NullHandlingDecoder<LinkedHashMap<*, *>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(LinkedHashMap::class.starProjectedType) ||
      type.isSubtypeOf(LinkedHashMap::class.starProjectedType.withNullability(true))

  override fun safeDecode(node: Node,
                          type: KType,
                          context: DecoderContext): ConfigResult<LinkedHashMap<*, *>> {
    require(type.arguments.size == 2)

    val kType = type.arguments[0].type!!
    val vType = type.arguments[1].type!!

    fun <K, V> decode(node: MapNode,
                      kdecoder: Decoder<K>,
                      vdecoder: Decoder<V>,
                      context: DecoderContext): ConfigResult<LinkedHashMap<K, V>> {

      return node.map.entries.map { (k, v) ->
        kdecoder.decode(StringNode(k, node.pos, node.path), kType, context).flatMap { kk ->
          vdecoder.decode(v, vType, context).map { vv ->
            kk to vv
          }
        }
      }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { pairs ->
          val map = linkedMapOf<K, V>()
          pairs.forEach { map[it.first] = it.second }
          map
        }
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
