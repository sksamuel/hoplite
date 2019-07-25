package com.sksamuel.hoplite.converter

import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.getOrElse
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveCursor
import com.sksamuel.hoplite.StringValue
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
              override fun apply(cursor: Cursor): ConfigResult<List<T>> {
                return when (val v = cursor.value()) {
                  is StringValue -> v.value.split(",").map { it.trim() }.map {
                    converter.apply(PrimitiveCursor("",
                        StringValue(it, Pos.NoPos),
                        emptyList()))
                  }.sequence()
                  is ListValue -> v.values.map { converter.apply(Cursor("", it, emptyList())) }.sequence()
                  else -> ConfigFailure("Unsupported list type ${v.javaClass.name}").invalidNel()
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
              override fun apply(cursor: Cursor): ConfigResult<Map<*, *>> {
                return when (val v = cursor.value()) {
                  is MapValue -> v.map.map { (k, v) ->
                    arrow.data.extensions.validated.applicative.map(
                        NonEmptyList.semigroup(),
                        kc.apply(Cursor("", StringValue(k, Pos.NoPos), emptyList())),
                        vc.apply(Cursor("", v, emptyList()))
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
