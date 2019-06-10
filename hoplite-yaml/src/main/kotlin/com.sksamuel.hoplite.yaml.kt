package com.sksamuel.hoplite.yaml

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.validNel
import com.sksamuel.hoplite.CannotParse
import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLocation
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.flatMap
import com.sksamuel.hoplite.readers.Reader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.error.MarkedYAMLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Attempts to load config from /application.yml on the resource path and returns
 * an instance of <A> if the values can be appropriately converted.
 */
inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig("")

inline fun <reified A : Any> loadConfig(resourceName: String): ConfigResult<A> {

  val yaml = Yaml(SafeConstructor())
  val loader = object {}

  return loader.javaClass.getResource(resourceName).toOption().fold(
      { ConfigFailure("Could not find resource $resourceName").invalidNel() },
      { it.validNel() }
  ).flatMap { resource ->
    handleYamlErrors(Paths.get(resource.toURI())) {
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
}

fun <A> handleYamlErrors(path: Path, f: (java.io.Reader) -> ConfigResult<A>): ConfigResult<A> =
    try {
      val r = Files.newBufferedReader(path)
      f(r)
    } catch (e: MarkedYAMLException) {
      CannotParse(e.message!!, locationFromMark(path, e.problemMark)).invalidNel()
    } catch (t: Throwable) {
      ConfigFailure.throwable(t).invalidNel()
    }

fun locationFromMark(path: Path, mark: Mark): ConfigLocation = ConfigLocation(path.toUri().toURL(), mark.line)

class MapCursor(private val map: Map<*, *>) : ConfigCursor {
  override fun value(): Any? = map
  override fun pathElems(): List<String> = emptyList()
  override fun location(): ConfigLocation? = null
  override fun isUndefined(): Boolean = false
  override fun isNull(): Boolean = false
  override fun atPath(path: String): ConfigResult<ConfigCursor> {
    return when (val el = map[path]) {
      null -> ConfigFailure.missingPath(path).invalidNel()
      is Map<*, *> -> MapCursor(el).validNel()
      else -> PrimitiveCursor(el).validNel()
    }
  }
}

class PrimitiveCursor(private val value: Any?) : ConfigCursor {
  override fun value(): Any? = value
}