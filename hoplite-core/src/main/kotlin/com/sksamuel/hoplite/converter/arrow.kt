package com.sksamuel.hoplite.converter

import arrow.data.NonEmptyList
import arrow.data.getOrElse
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveCursor
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class NonEmptyListConverterProvider : ConverterProvider {
  override fun <T : Any> provide(type: KType): Converter<T>? {
    if (type.isSubtypeOf(NonEmptyList::class.starProjectedType)) {
      if (type.arguments.size == 1) {
        val t = type.arguments[0].type
        if (t != null) {
          return locateConverter<T>(t).map { converter ->
            object : Converter<NonEmptyList<T>> {
              override fun apply(cursor: Cursor): ConfigResult<NonEmptyList<T>> {
                return when (val v = cursor.value()) {
                  is StringValue ->
                    v.value.split(",").map { it.trim() }.map {
                      converter.apply(PrimitiveCursor("",
                          StringValue(it, Pos.NoPos), emptyList()))
                    }.sequence().map {
                      NonEmptyList.fromListUnsafe(it)
                    }
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