package com.sksamuel.hoplite.decoder

import arrow.data.NonEmptyList
import arrow.data.getOrElse
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.StringValue
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class NonEmptyListDecoderFactory : DecoderFactory {
  override fun <T : Any> provide(type: KType): Decoder<T>? {
    if (type.isSubtypeOf(NonEmptyList::class.starProjectedType)) {
      if (type.arguments.size == 1) {
        val t = type.arguments[0].type
        if (t != null) {
          return locateConverter<T>(t).map { converter ->
            object : Decoder<NonEmptyList<T>> {
              override fun convert(value: Value): ConfigResult<NonEmptyList<T>> {
                return when (value) {
                  is StringValue ->
                    value.value.split(",").map { it.trim() }.map {
                      converter.convert(StringValue(it, value.pos))
                    }.sequence().map {
                      NonEmptyList.fromListUnsafe(it)
                    }
                  else -> ConfigFailure("Unsupported list type $value").invalidNel()
                }
              }
            }
          }.getOrElse { null } as Decoder<T>
        }
      }
    }
    return null
  }
}