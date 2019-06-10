package com.sksamuel.hoplite.converter

import arrow.data.NonEmptyList
import arrow.data.Validated
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.extensions.validated.applicative.applicative
import arrow.data.fix
import arrow.data.invalid
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import kotlin.reflect.KClass

object DataClassConverterProvider : ConverterProvider {
  override fun <T : Any> provide(targetType: KClass<T>): Converter<T>? {
    return if (targetType.isData) DataClassConverter(targetType) else null
  }
}

class DataClassConverter<T : Any>(private val klass: KClass<T>) : Converter<T> {

  override fun apply(cursor: Cursor): ConfigResult<T> {

    val args = klass.constructors.first().parameters.map { param ->

      val paramCursor = cursor.atPath(param.name!!)
      val converter = locateConverter(param.type)

      Validated.applicative(NonEmptyList.semigroup<ConfigFailure>()).map(paramCursor, converter) { (c, converter) ->
        converter.apply(c)
      }.fix().fold({ it.invalid() }, { it })
    }

    println("******")
    println("args=$args")

    val instance = args.sequence(Validated.applicative(NonEmptyList.semigroup<ConfigFailure>())).fix().map { it.fix() }.map {
      klass.constructors.first().call(*it.toTypedArray()) as T
    }

    return instance
  }
}