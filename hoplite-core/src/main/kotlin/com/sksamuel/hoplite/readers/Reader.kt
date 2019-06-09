package com.sksamuel.hoplite.readers

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface Reader<T> {

  fun read(cursor: ConfigCursor): ConfigResult<T>
  fun supports(c: KClass<*>): Boolean {
    val readerType = this.javaClass.genericInterfaces.filterIsInstance<ParameterizedType>().find { it.rawType == Reader::class.java }!!
    return readerType.actualTypeArguments[0].typeName == c.java.name
  }

  companion object {

    fun forType(type: KType): ConfigResult<Reader<*>> {
      val readers = ServiceLoader.load(Reader::class.java).toList()
      return when (val c = type.classifier) {
        is KClass<*> -> readers.toList().firstOrNull { it.supports(c) }.toOption().fold(
            { ConfigFailure.unsupportedType(type).invalidNel() },
            { it.validNel() }
        )
        else -> ConfigFailure("Unsupported ktype $type").invalidNel()
      }
    }

    /**
     * Returns a [Reader] for type T.
     */
    inline fun <reified T : Any> forT() = DataClassReader(T::class)
  }
}