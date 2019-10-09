@file:Suppress("unused")

package com.sksamuel.hoplite

import arrow.data.valueOr
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.defaultPreprocessors
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigException(msg: String) : java.lang.RuntimeException(msg)

class ConfigLoader(private val decoderRegistry: DecoderRegistry = defaultDecoderRegistry(),
                   private val sources: List<PropertySource> = defaultPropertySources(),
                   private val parserRegistry: ParserRegistry = defaultParserRegistry(),
                   private val preprocessors: List<Preprocessor> = defaultPreprocessors(),
                   private val keyMappers: List<KeyMapper> = defaultKeyMappers()) {

  fun withPreprocessor(preprocessor: Preprocessor) = ConfigLoader(
    decoderRegistry,
    sources,
    parserRegistry,
    preprocessors + preprocessor,
    keyMappers)

  fun withDecoder(decoder: Decoder<*>) = ConfigLoader(
    decoderRegistry.register(decoder),
    sources,
    parserRegistry,
    preprocessors,
    keyMappers)

  fun withFileExtensionMapping(ext: String, parser: Parser) = ConfigLoader(
    decoderRegistry,
    sources,
    parserRegistry.register(ext, parser),
    preprocessors,
    keyMappers)

  fun withKeyMapper(mapper: KeyMapper) = ConfigLoader(
    decoderRegistry,
    sources,
    parserRegistry,
    preprocessors,
    keyMappers + mapper)

  fun withPropertySource(source: PropertySource) = ConfigLoader(
    decoderRegistry,
    sources + source,
    parserRegistry,
    preprocessors,
    keyMappers)

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A = loadConfigOrThrow(resources.toList())

  inline fun <reified A : Any> loadConfigOrThrow(resources: List<String>): A = loadConfig<A>(resources).returnOrThrow()

  inline fun <reified A : Any> loadConfigOrThrow(): A = loadConfig(A::class, emptyList()).returnOrThrow()

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> = loadConfig(resources.toList())

  @JvmName("loadConfigFromResources")
  inline fun <reified A : Any> loadConfig(resources: List<String>): ConfigResult<A> {
    require(A::class.isData) { "Can only decode into data classes [was ${A::class}]" }
    return FileSource.fromClasspathResources(resources.toList()).flatMap { loadConfig(A::class, it) }
  }

  fun loadNodeOrThrow(resources: List<String>): Value =
    FileSource.fromClasspathResources(resources.toList()).flatMap { loadNode(it) }.returnOrThrow()

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg paths: Path): A = loadConfigOrThrow(paths.toList())

  @JvmName("loadConfigOrThrowFromPaths")
  inline fun <reified A : Any> loadConfigOrThrow(paths: List<Path>): A = loadConfig<A>(paths).returnOrThrow()

  @JvmName("loadNodeOrThrowFromPaths")
  fun loadNodeOrThrow(paths: List<Path>): Value =
    FileSource.fromPaths(paths.toList()).flatMap { loadNode(it) }.returnOrThrow()

  /**
   * Attempts to load config from the specified Paths and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg paths: Path): ConfigResult<A> = loadConfig(paths.toList())

  @JvmName("loadConfigFromPaths")
  inline fun <reified A : Any> loadConfig(paths: List<Path>): ConfigResult<A> {
    require(A::class.isData) { "Can only decode into data classes [was ${A::class}]" }
    return FileSource.fromPaths(paths.toList()).flatMap { loadConfig(A::class, it) }
  }

  fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.valueOr {
    val err = "Error loading config because:\n\n" + it.description().prependIndent(Constants.indent)
    throw ConfigException(err)
  }

  fun <A : Any> loadConfig(klass: KClass<A>, inputs: List<FileSource>): ConfigResult<A> {
    fun Value.decode() = decoderRegistry.decoder(klass).flatMap { decoder ->
      decoder.decode(this, klass.createType(), decoderRegistry)
    }
    return loadNode(inputs).flatMap { it.decode() }
  }

  private fun loadNode(inputs: List<FileSource>): ConfigResult<Value> {

    fun Value.preprocess() = preprocessors.fold(this) { node, preprocessor -> node.transform(preprocessor::process) }
    fun Value.keymapped() = keyMappers.fold(this) { node, mapper -> node.mapKey(mapper::map) }

    fun List<Value>.preprocessAll() = this.map { it.preprocess() }
    fun List<Value>.keyMapAll() = this.map { it.keymapped() }

    val sources = defaultPropertySources() + inputs.map { ConfigFilePropertySource(it, parserRegistry) }
    return sources.map { it.node() }.sequence()
      .map { it.preprocessAll() }
      .map { it.keyMapAll() }
      .map { it.reduce { acc, b -> acc.withFallback(b) } }
      .leftMap { ConfigFailure.MultipleFailures(it) }
  }
}

