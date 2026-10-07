package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.denormalize
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

class MapDecoder : NullHandlingDecoder<Map<*, *>> {

  override fun supports(type: KType): Boolean =
    type.isSubtypeOf(Map::class.starProjectedType) ||
      type.isSubtypeOf(Map::class.starProjectedType.withNullability(true))

  override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<Map<*, *>> {
    require(type.arguments.size == 2)

    val kType = type.arguments[0].type!!
    val vType = type.arguments[1].type!!

    fun <K, V> decodeFromMap(node: MapNode,
                             kdecoder: Decoder<K>,
                             vdecoder: Decoder<V>,
                             context: DecoderContext): ConfigResult<Map<*, *>> {

      return node.denormalize().map.entries.map { (k, v) ->
        kdecoder.decode(StringNode(k, node.pos, node.path, emptyMap()), kType, context).flatMap { kk ->
          vdecoder.decode(v, vType, context).map { vv ->
            context.usedPaths.add(v.path)
            kk to vv
          }
        }
      }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toMap() }
    }

    fun <K, V> decodeFromArray(node: ArrayNode,
                               kdecoder: Decoder<K>,
                               vdecoder: Decoder<V>,
                               context: DecoderContext): ConfigResult<Map<*, *>> {

      return node.elements.map { el ->
        val keyNode = el.atKey("key")
        val valueNode = el.atKey("value")
        kdecoder.decode(keyNode, kType, context).flatMap { kk ->
          vdecoder.decode(valueNode, vType, context).map { vv ->
            // Mark both subnodes as used so strict mode does not flag the array-of-objects
            // map form (`[{key: ..., value: ...}, ...]`) as containing unused entries —
            // matches what decodeFromMap above does for the value path.
            context.usedPaths.add(keyNode.path)
            context.usedPaths.add(valueNode.path)
            kk to vv
          }
        }
      }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { it.toMap() }
    }

    return context.decoder(kType).flatMap { kdecoder ->
      context.decoder(vType).flatMap { vdecoder ->
        when (node) {
          is MapNode -> decodeFromMap(node, kdecoder, vdecoder, context)
          is ArrayNode -> decodeFromArray(node, kdecoder, vdecoder, context)
          else -> ConfigFailure.UnsupportedCollectionType(node, "Map").invalid()
        }
      }
    }
  }
}
