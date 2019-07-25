package com.sksamuel.hoplite.converter

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

class ListConverterProvider : ConverterProvider {
  override fun <T : Any> provide(type: KType): Converter<T>? {
    if (type.isSubtypeOf(List::class.starProjectedType)) {
      if (type.arguments.size == 1) {
        val t = type.arguments[0].type
        if (t != null) {
          return locateConverter<T>(t).map { converter ->
            object : Converter<List<T>> {
              override fun apply(value: Value): ConfigResult<List<T>> {
                return when (value) {
                  is StringValue -> value.value.split(",").map { it.trim() }.map {
                    converter.apply(StringValue(it, value.pos))
                  }.sequence()
                  is ListValue -> value.values.map { converter.apply(it) }.sequence()
                  else -> ConfigFailure("Unsupported list type ${value.javaClass.name}").invalidNel()
                }
              }
            }
          }.getOrElse { null } as Converter<T>
        }
      }
    }
    return null
  }
}

class MapConverterProvider : ConverterProvider {
  override fun <T : Any> provide(type: KType): Converter<T>? {
    if (type.isSubtypeOf(Map::class.starProjectedType)) {
      if (type.arguments.size == 2) {

        val k = type.arguments[0].type
        val v = type.arguments[1].type
        if (k != null && v != null) {

          val keyConverter = locateConverter<Any>(k)
          val valueConverter = locateConverter<Any>(v)

          return arrow.data.extensions.validated.applicative.map(
              NonEmptyList.semigroup(),
              keyConverter,
              valueConverter) { (kc, vc) ->
            object : Converter<Map<*, *>> {
              override fun apply(value: Value): ConfigResult<Map<*, *>> {
                return when (value) {
                  is MapValue -> value.map.map { (k, v) ->
                    arrow.data.extensions.validated.applicative.map(
                        NonEmptyList.semigroup(),
                        kc.apply(StringValue(k, value.pos)),
                        vc.apply(v)
                    ) { (k, v) -> Pair(k, v) }
                  }.sequence().map { it.toMap() }
                  else -> ConfigFailure("Unsupported map type $v").invalidNel()
                }
              }
            }
          }.getOrElse { null } as Converter<T>
        }
      }
    }
    return null
  }
}
