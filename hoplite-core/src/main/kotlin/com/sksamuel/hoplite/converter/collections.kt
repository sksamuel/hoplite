package com.sksamuel.hoplite.converter

import arrow.data.NonEmptyList
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.getOrElse
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.PrimitiveCursor
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
                  is String -> v.split(",").map { it.trim() }.map { converter.apply(PrimitiveCursor(it)) }.sequence()
                  else -> ConfigFailure("Unsupported list type $v").invalidNel()
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
        val v = type.arguments[0].type
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
                  is Map<*, *> -> v.validNel()
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
