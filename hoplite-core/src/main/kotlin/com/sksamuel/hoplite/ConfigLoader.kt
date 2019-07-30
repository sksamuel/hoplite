package com.sksamuel.hoplite

import arrow.core.Try
import arrow.core.toOption
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.defaultPreprocessors
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigException(msg: String) : java.lang.RuntimeException(msg)
data class InputSource(val resource: String, val stream: InputStream)

class ConfigLoader(private val decoderRegistry: DecoderRegistry = defaultDecoderRegistry(),
                   private val parserRegistry: ParserRegistry = defaultParserRegistry(),
                   private val preprocessors: List<Preprocessor> = defaultPreprocessors()) {

  fun withPreprocessor(preprocessor: Preprocessor) = ConfigLoader(
    decoderRegistry,
    parserRegistry,
    preprocessors + preprocessor)

  fun withDecoder(decoder: Decoder<*>) = ConfigLoader(
    decoderRegistry.register(decoder),
    parserRegistry,
    preprocessors)

  fun withFileExtensionMapping(ext: String, parser: Parser) = ConfigLoader(
    decoderRegistry,
    parserRegistry.register(ext, parser),
    preprocessors)

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A = loadConfig<A>(*resources).returnOrThrow()

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> {
    require(A::class.isData) { "Can only decode into data classes [was ${A::class}]" }
    return resources.map { resource ->
      this.javaClass.getResourceAsStream(resource).toOption().fold(
        { ConfigFailure.UnknownSource(resource).invalid() },
        { InputSource(resource, it).valid() }
      )
    }.sequence()
      .leftMap { ConfigFailure.MultipleFailures(it) }
      .flatMap { loadConfig(A::class, it) }
  }


  /**
   * Attempts to load config from the specified Paths and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg paths: Path): ConfigResult<A> {
    require(A::class.isData) { "Can only decode into data classes [was ${A::class}]" }
    return paths.map { path ->
      Try { Files.newInputStream(path) }.fold(
        { ConfigFailure.UnknownSource(path.toString()).invalid() },
        { InputSource(path.toString(), it).valid() }
      )
    }.sequence()
      .leftMap { ConfigFailure.MultipleFailures(it) }
      .flatMap { loadConfig(A::class, it) }
  }

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg paths: Path): A = loadConfig<A>(*paths).returnOrThrow()

  fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.fold(
    {
      val err = "Error loading config because:\n\n" + it.description().prependIndent("    ")
      throw ConfigException(err)
    },
    { it }
  )

  fun <A : Any> loadConfig(klass: KClass<A>, sources: List<InputSource>): ConfigResult<A> {
    val path = klass.java.name

    fun InputSource.ext() = this.resource.split('.').last()
    fun InputSource.parse() = parserRegistry.locate(ext()).map { it.load(stream, resource) }
    fun Node.preprocess() = preprocessors.fold(this) { acc, p -> acc.transform(p::process) }
    fun List<Node>.preprocessAll() = this.map { it.preprocess() }
    fun Node.decode() = decoderRegistry.decoder(klass, path).flatMap { decoder ->
      decoder.decode(this, klass.createType(), decoderRegistry, path)
    }

    return sources.map { it.parse() }.sequence()
      .map { it.preprocessAll() }
      .map { it.reduce { acc, b -> acc.withFallback(b) } }
      .leftMap { ConfigFailure.MultipleFailures(it) }
      .flatMap { it.decode() }
  }
}

