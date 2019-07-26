package com.sksamuel.hoplite.decoder

import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.getOrElse
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class ListDecoder<T>(val decoder: Decoder<T>) : Decoder<List<T>> {
  override fun convert(value: Value): ConfigResult<List<T>> {
    return when (value) {
      is ListValue -> value.values.map { decoder.convert(it) }.sequence()
      is StringValue -> {
        val tokens = value.value.split(",").map { it.trim() }
        tokens.map { decoder.convert(StringValue(it, value.pos)) }.sequence()
      }
      else -> ConfigFailure("Unsupported list type ${value.javaClass.name}").invalidNel()
    }
  }
}

class ListDecoderFactory : DecoderFactory {
  override fun build(type: KType, registry: DecoderRegistry): Decoder<out List<Any?>>? {
    return if (type.isSubtypeOf(List::class.starProjectedType)) {
      // must have 1 type arg for a list
      require(type.arguments.size == 1)
      val t = type.arguments[0].type!!
      registry.decoder(t).fold({ null }, { ListDecoder(it) })
    } else null
  }
}

fun <K, V> mapDecoder(kdecoder: Decoder<K>, vdecoder: Decoder<V>) = object : Decoder<Map<K, V>> {
  override fun convert(value: Value): ConfigResult<Map<K, V>> {
    return when (value) {
      is MapValue -> value.map.map { (k, v) ->
        arrow.data.extensions.validated.applicative.map(
            NonEmptyList.semigroup(),
            kc.convert(StringValue(k, value.pos)),
            vc.convert(v)
        ) { (k, v) -> Pair(k, v) }
      }.sequence().map { it.toMap() }
      else -> ConfigFailure("Unsupported map type $v").invalidNel()
    }
  }
}

class MapDecoderFactory : DecoderFactory {
  override fun build(type: KType, registry: DecoderRegistry): Decoder<*>? {
  }
//    if (type.isSubtypeOf(Map::class.starProjectedType)) {
//      if (type.arguments.size == 2) {
//
//        val k = type.arguments[0].type
//        val v = type.arguments[1].type
//        if (k != null && v != null) {
//
//          val keyConverter = locateConverter(k)
//          val valueConverter = locateConverter(v)
//
//          return arrow.data.extensions.validated.applicative.map(
//              NonEmptyList.semigroup(),
//              keyConverter,
//              valueConverter) { (kc, vc) ->
//
//          }.getOrElse { null } as Decoder<*>
//        }
//      }
//    }
}
