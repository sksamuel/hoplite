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
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface Reader<T> {

  fun read(cursor: ConfigCursor): ConfigResult<T>

  companion object {

    object StringReader : Reader<String> {
      override fun read(cursor: ConfigCursor): ConfigResult<String> = cursor.asString()
    }

    object LongReader : Reader<Long> {
      override fun read(cursor: ConfigCursor): ConfigResult<Long> = cursor.asLong()
    }

    object DoubleReader : Reader<Double> {
      override fun read(cursor: ConfigCursor): ConfigResult<Double> = cursor.asDouble()
    }

    object FloatReader : Reader<Float> {
      override fun read(cursor: ConfigCursor): ConfigResult<Float> = cursor.asFloat()
    }

    object IntReader : Reader<Int> {
      override fun read(cursor: ConfigCursor): ConfigResult<Int> = cursor.asInt()
    }

    object BooleanReader : Reader<Boolean> {
      override fun read(cursor: ConfigCursor): ConfigResult<Boolean> = cursor.asBoolean()
    }

    fun forType(type: KType): ConfigResult<Reader<*>> {
      return when (type.classifier) {
        String::class -> StringReader.validNel()
        Boolean::class -> BooleanReader.validNel()
        Int::class -> IntReader.validNel()
        Long::class -> LongReader.validNel()
        Float::class -> FloatReader.validNel()
        Double::class -> DoubleReader.validNel()
        else -> ConfigFailure("Unsupported type ${type.classifier}").invalidNel()
      }
    }

    /**
     * Returns a [Reader] for type T.
     */
    inline fun <reified T : Any> forT() = DataClassReader(T::class)
  }
}

class DataClassReader<T : Any>(private val klass: KClass<T>) : Reader<T> {

  override fun read(cursor: ConfigCursor): ConfigResult<T> {

    return if (klass.isData) {

      val args = klass.constructors.first().parameters.map { param ->

        val paramCursor = cursor.atPath(param.name!!)
        val reader = Reader.forType(param.type)

        Validated.applicative(NonEmptyList.semigroup<ConfigFailure>()).map(paramCursor, reader) { (c, r) ->
          r.read(c)
        }.fix().fold({ it.invalid() }, { it })
      }

      println("******")
      println("args=$args")

      args.sequence(Validated.applicative(NonEmptyList.semigroup<ConfigFailure>())).fix().map { it.fix() }.map {
        klass.constructors.first().call(*it.toTypedArray())
      }

    } else {
      ConfigFailure("Cannot read values for a non-data class").invalidNel()
    }
  }
}


