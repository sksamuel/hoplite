package com.sksamuel.hoplite

import arrow.data.NonEmptyList
import arrow.data.Validated
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.extensions.validated.applicative.applicative
import arrow.data.fix
import arrow.data.invalid
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

            val paramCursor = cursor.atPath(param.name!!)
            val reader = forType(param.type)

            Validated.applicative(NonEmptyList.semigroup<ConfigFailure>()).map(paramCursor, reader) { (c, r) ->
              r.read(c)
            }.fix().fold({ it.invalid() }, { it })
          }

          args.sequence(Validated.applicative(NonEmptyList.semigroup<ConfigFailure>())).fix().map { it.fix() }.map {
            T::class.constructors.first().call(it.toList())
          }

        } else {
          ConfigFailure("Cannot read values for a non-data class").invalidNel()
        }
      }
    }
  }
}


