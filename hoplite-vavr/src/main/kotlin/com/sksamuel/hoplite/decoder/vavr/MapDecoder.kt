package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import io.vavr.collection.LinkedHashMap
import io.vavr.collection.Map
import io.vavr.kotlin.tuple
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

    fun <K, V> decodeFromMap(node: MapNode,
                             kdecoder: Decoder<K>,
                             vdecoder: Decoder<V>,
                             context: DecoderContext): ConfigResult<Map<*, *>> =
      node.map.entries.map { (k, v) ->
        kdecoder.decode(StringNode(k, node.pos), kType, context).flatMap { kk ->
          vdecoder.decode(v, vType, context).map { vv ->
            kk to vv
          }
        }
      }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { LinkedHashMap.ofEntries(it.map { pair -> pair.tuple() }) }

    fun <K, V> decodeFromArray(node: ArrayNode,
                               kdecoder: Decoder<K>,
                               vdecoder: Decoder<V>,
                               context: DecoderContext): ConfigResult<Map<*, *>> =
      node.elements.map { el ->
        kdecoder.decode(el.atKey("key"), kType, context).flatMap { kk ->
          vdecoder.decode(el.atKey("value"), vType, context).map { vv ->
            kk to vv
          }
        }
      }.sequence()
        .mapInvalid { ConfigFailure.CollectionElementErrors(node, it) }
        .map { LinkedHashMap.ofEntries(it.map { pair -> pair.tuple() }) }

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
