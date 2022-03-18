package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import com.sksamuel.hoplite.preprocessor.EnvVarPreprocessor
import com.sksamuel.hoplite.preprocessor.LookupPreprocessor
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.RandomPreprocessor
import com.sksamuel.hoplite.preprocessor.SystemPropertyPreprocessor
import java.util.ServiceLoader

class ConfigLoaderBuilder private constructor() {

  private val failureCallbacks = mutableListOf<(Throwable) -> Unit>()

  // this is the default class loader that ServiceLoader::load(Class<T>)
  // gets before delegating to ServiceLoader::load(Class<T>, ClassLoader)
  private var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

  private var mode: DecodeMode = DecodeMode.Lenient

  private val propertySources = mutableListOf<PropertySource>()
  private val preprocessors = mutableListOf<Preprocessor>()
  private val paramMappers = mutableListOf<ParameterMapper>()
  private val parsers = mutableMapOf<String, Parser>()
  private val decoders = mutableListOf<Decoder<*>>()

  companion object {

    /**
     * Returns a [ConfigLoaderBuilder] with all defaults applied.
     *
     * This means that the default [Decoder]s, [Preprocessor]s, [ParameterMapper]s, [PropertySource]s,
     * and [Parser]s are all registered.
     *
     * If you wish to avoid adding defaults, for example to avoid certain decoders or sources, then
     * use [empty] to obtain an empty ConfigLoaderBuilder and call the various addDefault methods manually.
     */
    fun default(): ConfigLoaderBuilder {
      return empty()
        .addDefaultDecoders()
        .addDefaultPreprocessors()
        .addDefaultParamMappers()
        .addDefaultPropertySources()
        .addDefaultParsers()
    }

    fun empty(): ConfigLoaderBuilder = ConfigLoaderBuilder()
  }

  fun addSource(propertySource: PropertySource) = addPropertySource(propertySource)
  fun addPropertySource(propertySource: PropertySource) = addPropertySources(listOf(propertySource))
  fun addPropertySources(propertySources: Iterable<PropertySource>): ConfigLoaderBuilder = apply {
    this.propertySources.addAll(propertySources)
  }

  fun addDefaultPropertySources() = apply {
    this.propertySources.addAll(defaultPropertySources())
  }


  fun addParameterMapper(paramMapper: ParameterMapper) = addParameterMappers(listOf(paramMapper))
  fun addParameterMappers(paramMappers: Iterable<ParameterMapper>): ConfigLoaderBuilder = apply {
    this.paramMappers.addAll(paramMappers)
  }

  fun addDefaultParamMappers() = apply {
    this.paramMappers.addAll(defaultParamMappers())
  }

  fun addDecoder(decoder: Decoder<*>) = addDecoders(listOf(decoder))
  fun addDecoders(decoders: Iterable<Decoder<*>>) = apply {
    this.decoders.addAll(decoders)
  }

  /**
   * Adds all default [Decoder]s into this builder.
   * The decoders are located via java's [ServiceLocator] framework.
   */
  fun addDefaultDecoders() = addDecoders(ServiceLoader.load(Decoder::class.java, classLoader))

  fun addPreprocessor(preprocessor: Preprocessor) = addPreprocessors(listOf(preprocessor))
  fun addPreprocessors(preprocessors: Iterable<Preprocessor>): ConfigLoaderBuilder = apply {
    this.preprocessors.addAll(preprocessors)
  }

  fun addDefaultPreprocessors() = addPreprocessors(defaultPreprocessors())

  fun addDefaultParsers() = apply {
    ServiceLoader.load(Parser::class.java, classLoader).toList().forEach { parser ->
      parser.defaultFileExtensions().forEach { ext -> addFileExtensionMapping(ext, parser) }
    }
  }

  fun addFileExtensionMapping(ext: String, parser: Parser): ConfigLoaderBuilder = apply {
    this.parsers[ext] = parser
  }

  fun addFileExtensionMappings(map: Map<String, Parser>): ConfigLoaderBuilder = apply {
    map.forEach {
      val (ext, parser) = it
      this.parsers[ext] = parser
    }
  }

  /**
   * Registers a callback that will be invoked with any exception generated when
   * the [loadConfigOrThrow] operation is used. The callback will be invoked immediately
   * before the exception is thrown.
   *
   * Note: [loadConfig] methods will not invoke this callback, instead, you can use the
   * functions available on the returned error.
   */
  fun addOnFailureCallback(f: (Throwable) -> Unit): ConfigLoaderBuilder = apply {
    this.failureCallbacks.add(f)
  }

  fun strict(): ConfigLoaderBuilder = apply {
    this.mode = DecodeMode.Strict
  }

  fun build(): ConfigLoader {

    val decoderRegistry = this.decoders.fold(defaultDecoderRegistry(this.classLoader)) { registry, decoder ->
      registry.register(decoder)
    }

    // build the DefaultParserRegistry
    val parserRegistry =
      this.parsers.asSequence().fold(defaultParserRegistry(this.classLoader)) { registry, (ext, parser) ->
        registry.register(ext, parser)
      }

    return ConfigLoader(
      decoderRegistry = decoderRegistry,
      propertySources = propertySources.toList(),
      parserRegistry = parserRegistry,
      preprocessors = preprocessors.toList(),
      paramMappers = paramMappers.toList(),
      onFailure = failureCallbacks.toList(),
      mode = mode,
    )
  }
}

fun defaultPropertySources(): List<PropertySource> = listOf(
  SystemPropertiesPropertySource,
  UserSettingsPropertySource
)

fun defaultPreprocessors() = listOf(
  EnvVarPreprocessor,
  SystemPropertyPreprocessor,
  RandomPreprocessor,
  LookupPreprocessor
)

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  SnakeCaseParamMapper,
  KebabCaseParamMapper,
  AliasAnnotationParamMapper,
)
