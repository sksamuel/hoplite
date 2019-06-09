package com.sksamuel.hoplite.readers

import arrow.data.NonEmptyList
import arrow.data.Validated
import arrow.data.extensions.list.traverse.sequence
import arrow.data.extensions.nonemptylist.semigroup.semigroup
import arrow.data.extensions.validated.applicative.applicative
import arrow.data.fix
import arrow.data.invalid
import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import kotlin.reflect.KClass

class DataClassReader<T : Any>(private val klass: KClass<T>) : Reader<T> {

  override fun supports(c: KClass<*>): Boolean = c.isData

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