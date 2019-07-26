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

class MapDecoder : Decoder<Map<*, *>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(Map::class.starProjectedType)

  private fun <K, V> decode(node: MapNode,
                            kType: KType,
                            vType: KType,
                            kdecoder: Decoder<K>,
                            vdecoder: Decoder<V>,
                            registry: DecoderRegistry): ConfigResult<Map<*, *>> {

    return node.map.entries.map { (k, v) ->
      arrow.data.extensions.validated.applicative.map(
          NonEmptyList.semigroup(),
          kdecoder.decode(StringNode(k, node.pos), kType, registry),
          vdecoder.decode(v, vType, registry)) { (a, b) -> a to b }
    }.sequence().map { it.toMap() }
  }

  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<Map<*, *>> {
    require(type.arguments.size == 2)

    val k = type.arguments[0].type!!
    val v = type.arguments[1].type!!

    return registry.decoder(k).flatMap { kdecoder ->
      registry.decoder(v).flatMap { vdecoder ->
        when (node) {
          is MapNode -> decode(node, k, v, kdecoder, vdecoder, registry)
          else -> ConfigFailure("Unsupported map type $v").invalidNel()
        }
      }
    }
  }
}