package com.sksamuel.hoplite.yaml

import arrow.core.toOption
import arrow.data.Invalid
import arrow.data.Valid
import arrow.data.Validated
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.BooleanConfigCursor
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLocation
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigValue
import com.sksamuel.hoplite.DoubleConfigCursor
import com.sksamuel.hoplite.FloatConfigCursor
import com.sksamuel.hoplite.IntConfigCursor
import com.sksamuel.hoplite.LongConfigCursor
import com.sksamuel.hoplite.readers.Reader
import com.sksamuel.hoplite.StringConfigCursor
import org.yaml.snakeyaml.Yaml

/**
 * Attempts to load config from /application.yml on the resource path and returns
 * an instance of <A> if the values can be appropriately converted.
 */
inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig("")

inline fun <reified A : Any> loadConfig(resourceName: String): ConfigResult<A> {

  val yaml = Yaml()
  val loader = object {}

  return loader.javaClass.getResourceAsStream(resourceName).toOption().fold(
      { ConfigFailure("Could not find resource $resourceName").invalidNel() },
      { it.validNel() }
  ).flatMap {
    val result = yaml.load<Any>(it)
    println("Result=$result")
    val cursor = when (result) {
      is Map<*, *> -> MapCursor(result)
      else -> TODO()
    }
    val reader = Reader.forT<A>()
    reader.read(cursor)
  }
}

fun <E, A, B> Validated<E, A>.flatMap(f: (A) -> Validated<E, B>): Validated<E, B> {
  return when (this) {
    is Invalid -> this
    is Valid -> f(this.a)
  }
}

class MapCursor(private val map: Map<*, *>) : ConfigCursor {
  override fun value(): ConfigValue = TODO()
  override fun pathElems(): List<String> = emptyList()
  override fun location(): ConfigLocation? = null
  override fun isUndefined(): Boolean = false
  override fun isNull(): Boolean = false
  override fun atPath(path: String): ConfigResult<ConfigCursor> {
    return when (val el = map[path]) {
      is String -> StringConfigCursor(el).validNel()
      is Boolean -> BooleanConfigCursor(el).validNel()
      is Int -> IntConfigCursor(el).validNel()
      is Long -> LongConfigCursor(el).validNel()
      is Float -> FloatConfigCursor(el).validNel()
      is Double -> DoubleConfigCursor(el).validNel()
      null -> ConfigFailure.missingPath(path).invalidNel()
      else -> ConfigFailure("Unsupported cursor $el").invalidNel()
    }
  }
}