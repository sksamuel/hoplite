package com.sksamuel.hoplite.converter

import arrow.data.ValidatedNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Value
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KClass

class DataClassConverter<T : Any>(private val klass: KClass<T>) : Converter<T> {

  override fun apply(value: Value): ConfigResult<T> {

    val args: ValidatedNel<ConfigFailure, List<Any?>> = klass.constructors.first().parameters.map { param ->
      converterFor(param.type).flatMap {
        it.apply(value.atKey(param.name!!))
      }
    }.sequence()

    return args.map {
      klass.constructors.first().call(*it.toTypedArray())
    }
  }
}