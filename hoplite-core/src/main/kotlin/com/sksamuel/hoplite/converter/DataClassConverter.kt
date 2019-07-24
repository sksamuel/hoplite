package com.sksamuel.hoplite.converter

import arrow.data.ValidatedNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor2
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass

class DataClassConverter<T : Any>(private val klass: KClass<T>) : Converter<T> {

  override fun apply(cursor: Cursor2): ConfigResult<T> {

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      converterFor(param.type).flatMap {
        it.apply(cursor.atKey(param.name!!))
      }
    }.sequence()

    return args.map {
      klass.constructors.first().call(*it.toTypedArray())
    }
  }
}