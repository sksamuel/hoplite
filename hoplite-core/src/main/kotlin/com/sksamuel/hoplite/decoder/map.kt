package com.sksamuel.hoplite.decoder

import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class MapDecoder : NonNullableDecoder<Map<*, *>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(Map::class.starProjectedType)

  override fun safeDecode(node: Node,
                      type: KType,
                      registry: DecoderRegistry,
                      path: String): ConfigResult<Map<*, *>> {
    require(type.arguments.size == 2)

    val kType = type.arguments[0].type!!
    val vType = type.arguments[1].type!!

    fun <K, V> decode(node: MapNode,
                      kdecoder: Decoder<K>,
                      vdecoder: Decoder<V>,
                      registry: DecoderRegistry): ConfigResult<Map<*, *>> {

      return node.map.entries.map { (k, v) ->
        arrow.data.extensions.validated.applicative.map(
          NonEmptyList.semigroup(),
          kdecoder.decode(StringNode(k, node.pos, node.dotpath), kType, registry, path),
          vdecoder.decode(v, vType, registry, path)) { (a, b) -> a to b }
      }.sequence().map { it.toMap() }
    }

    return registry.decoder(kType, path).flatMap { kdecoder ->
      registry.decoder(vType, path).flatMap { vdecoder ->
        when (node) {
          is MapNode -> decode(node, kdecoder, vdecoder, registry)
          else -> ConfigFailure("Unsupported map type $vType").invalidNel()
        }
      }
    }
  }
}
