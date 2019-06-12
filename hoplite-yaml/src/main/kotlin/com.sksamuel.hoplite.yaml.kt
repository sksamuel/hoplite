package com.sksamuel.hoplite.yaml

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.valid
import arrow.data.validNel
import com.sksamuel.hoplite.CannotParse
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLocation
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor
import com.sksamuel.hoplite.MapCursor
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.converter.DataClassConverter
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.error.MarkedYAMLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Attempts to load config from /application.yml on the resource path and returns
 * an instance of <A> if the values can be appropriately converted.
 */
inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig("/application.yml")

inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A =
    loadConfig<A>(*resources).fold(
        { errors -> throw RuntimeException("Error loading config\n" + errors.all.joinToString("\n") { it.description() }) },
        { it }
    )

inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> {
  val loader = object {}

  val uris = resources.map { resource ->
    loader.javaClass.getResource(resource).toOption().fold(
        { ConfigFailure("Could not find resource $resource").invalidNel() },
        { it.valid() }
    )
  }.sequence()

  val cursors = uris.flatMap {
    it.map { uri -> toCursor(uri) }.sequence()
  }

  return cursors.map {
    it.reduce { a, b -> a.withFallback(b) }
  }.flatMap {
    DataClassConverter(A::class).apply(it)
  }
}

fun toCursor(url: URL): ConfigResult<Cursor> = handleYamlErrors(Paths.get(url.toURI())) {
  val yaml = Yaml(SafeConstructor())
  val result = yaml.load<Any>(it)
  println("Result=$result")
  when (result) {
    is Map<*, *> -> MapCursor(result).validNel()
    else -> ConfigFailure("Unsupported YAML return type ${result.javaClass.name}").invalidNel()
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