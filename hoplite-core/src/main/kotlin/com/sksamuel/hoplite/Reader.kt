package com.sksamuel.hoplite

import arrow.data.NonEmptyList
import arrow.data.Validated
import arrow.data.ValidatedNel
import arrow.data.invalidNel
import arrow.data.validNel
import kotlin.reflect.KType

interface Reader<T> {

  fun read(cursor: ConfigCursor): ConfigResult<T>

  companion object {

    object StringReader : Reader<String> {
      override fun read(cursor: ConfigCursor): ConfigResult<String> = cursor.asString()
    }

    object LongReader : Reader<String> {
      override fun read(cursor: ConfigCursor): ConfigResult<String> = cursor.asString()
    }

    object BooleanReader : Reader<String> {
      override fun read(cursor: ConfigCursor): ConfigResult<String> = cursor.asString()
    }

    fun forType(type: KType): ConfigResult<Reader<*>> {
      return when (type.classifier) {
        String::class -> StringReader.validNel()
        Boolean::class -> BooleanReader.validNel()
        Long::class -> LongReader.validNel()
        else -> ConfigFailure("Unsupported type ${type.classifier}").invalidNel()
      }
    }

    /**
     * Returns a [Reader] for type T.
     */
    inline fun <reified T> forT() = object : Reader<T> {
      override fun read(cursor: ConfigCursor): ConfigResult<T> {
        return if (T::class.isData) {

          val args = T::class.constructors.first().parameters.map { param ->

            val cursor2 = cursor.atPath(param.name!!)
            val reader = forType(param.type)

            when {
              cursor2 is Validated.Valid && reader is Validated.Valid -> reader.a.read(cursor2.a)
              cursor2 is Validated.Valid && reader is Validated.Invalid -> reader.toValidatedNel()
              cursor2 is Validated.Invalid && reader is Validated.Valid -> cursor2.toValidatedNel()
              cursor2 is Validated.Invalid && reader is Validated.Invalid ->
                Validated.Invalid(NonEmptyList(cursor2.e, listOf(reader.e)))
              else -> throw IllegalStateException("Not possible value")
            }
          }

          // todo the args must be flatten
          val flattened: ValidatedNel<ConfigFailure, List<Any?>> = emptyList<Any?>().validNel()
          flattened.map { aa -> T::class.constructors.first().call(aa) }
        } else {
          ConfigFailure("Cannot read values for a non-data class").invalidNel()
        }
      }
    }
  }
}


