@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.defaultPreprocessors
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader constructor(
  private val decoderRegistry: DecoderRegistry,
  private val propertySources: List<PropertySource>,
  private val parserRegistry: ParserRegistry,
  private val preprocessors: List<Preprocessor>,
  private val paramMappers: List<ParameterMapper>,
  private val onFailure: List<(Throwable) -> Unit> = emptyList(),
  private val mode: DecodeMode = DecodeMode.Lenient,
) {

  companion object {
    operator fun invoke(): ConfigLoader {
      return Builder().build()
    }

    inline operator fun invoke(block: Builder.() -> Unit): ConfigLoader {
      return Builder().apply(block).build()
    }
  }

  @Deprecated(
    message = "Please use the ConfigLoader.Builder instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
      "Builder().addPreprocessor(preprocessor).build()",
      "com.sksamuel.hoplite.ConfigLoader.Builder"
    )
  )
  fun withPreprocessor(preprocessor: Preprocessor): ConfigLoader {
    return Builder().addPreprocessor(preprocessor).build()
  }

  @Deprecated(
    message = "Please use the ConfigLoader.Builder instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
      "Builder().addDecoder(decoder).build()",
      "com.sksamuel.hoplite.ConfigLoader.Builder"
    )
  )
  fun withDecoder(decoder: Decoder<*>): ConfigLoader {
    return Builder().addDecoder(decoder).build()
  }

  @Deprecated(
    message = "Please use the ConfigLoader.Builder instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
      "Builder().addFileExtensionMapping(ext, parser).build()",
      "com.sksamuel.hoplite.ConfigLoader.Builder"
    )
  )
  fun withFileExtensionMapping(ext: String, parser: Parser): ConfigLoader {
    return Builder().addFileExtensionMapping(ext, parser).build()
  }

  @Deprecated(
    message = "Please use the ConfigLoader.Builder instead.",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
      "Builder().addParameterMapper(mapper).build()",
      "com.sksamuel.hoplite.ConfigLoader.Builder"
    )
  )
  fun withParameterMapper(mapper: ParameterMapper): ConfigLoader {
    return Builder().addParameterMapper(mapper).build()
  }

  @Deprecated(
    message = "Please use the ConfigLoader.Builder instead.",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith(
      "Builder().addPropertySource(source).build()",
      "com.sksamuel.hoplite.ConfigLoader.Builder"
    )
  )
  fun withPropertySource(source: PropertySource): ConfigLoader {
    return Builder().addPropertySource(source).build()
  }

  class Builder {

    // this is the default class loader that ServiceLoader::load(Class<T>)
    // gets before delegating to ServiceLoader::load(Class<T>, ClassLoader)
    private var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

    private val decoderStaging = mutableListOf<Decoder<*>>()
    private val parserStaging = mutableMapOf<String, Parser>()
    private val propertySourceStaging = mutableListOf<PropertySource>()
    private val preprocessorStaging = mutableListOf<Preprocessor>()
    private val paramMapperStaging = mutableListOf<ParameterMapper>()
    private val failureCallbacks = mutableListOf<(Throwable) -> Unit>()
    private var mode: DecodeMode = DecodeMode.Lenient
    private var defaultSources = true
    private var defaultPreprocessors = true
    private var defaultParamMappers = true

    fun withDefaultSources(defaultSources: Boolean): Builder = apply {
      this.defaultSources = defaultSources
    }

    fun withDefaultPreprocessors(defaultPreprocessors: Boolean): Builder = apply {
      this.defaultPreprocessors = defaultPreprocessors
    }

    fun withDefaultParamMappers(defaultParamMappers: Boolean): Builder = apply {
      this.defaultParamMappers = defaultParamMappers
    }

    fun withClassLoader(classLoader: ClassLoader): Builder = apply {
      if (this.classLoader !== classLoader) {
        this.classLoader = classLoader
      }
    }

    fun addDecoder(decoder: Decoder<*>): Builder = apply {
      this.decoderStaging.add(decoder)
    }

    fun addDecoders(decoders: Iterable<Decoder<*>>): Builder = apply {
      this.decoderStaging.addAll(decoders)
    }

    fun addFileExtensionMapping(ext: String, parser: Parser): Builder = apply {
      this.parserStaging[ext] = parser
    }

    fun addFileExtensionMappins(map: Map<String, Parser>): Builder = apply {
      map.forEach {
        val (ext, parser) = it
        this.parserStaging[ext] = parser
      }
    }

    fun strict(): Builder = apply {
      this.mode = DecodeMode.Strict
    }

    fun addSource(source: PropertySource) = addPropertySource(source)

    fun addPropertySource(propertySource: PropertySource): Builder = apply {
      this.propertySourceStaging.add(propertySource)
    }

    fun addSources(sources: Iterable<PropertySource>) = addPropertySources(sources)

    fun addPropertySources(propertySources: Iterable<PropertySource>): Builder = apply {
      this.propertySourceStaging.addAll(propertySources)
    }

    fun addPreprocessor(preprocessor: Preprocessor): Builder = apply {
      this.preprocessorStaging.add(preprocessor)
    }

    fun addPreprocessors(preprocessors: Iterable<Preprocessor>): Builder = apply {
      this.preprocessorStaging.addAll(preprocessors)
    }

    fun addParameterMapper(paramMapper: ParameterMapper): Builder = apply {
      this.paramMapperStaging.add(paramMapper)
    }

    fun addParameterMappers(paramMappers: Iterable<ParameterMapper>): Builder = apply {
      this.paramMapperStaging.addAll(paramMappers)
    }

    /**
     * Registers a callback that will be invoked with any exception generated when
     * the [loadConfigOrThrow] operation is used. The callback will be invoked immediately
     * before the exception is thrown.
     *
     * Note: [loadConfig] methods will not invoke this callback, instead, you can use the
     * functions available on the returned error.
     */
    fun addOnFailureCallback(f: (Throwable) -> Unit): Builder {
      this.failureCallbacks.add(f)
      return this
    }

    fun build(): ConfigLoader {
      val decoderRegistry = this.decoderStaging.fold(defaultDecoderRegistry(this.classLoader)) { registry, decoder ->
        registry.register(decoder)
      }

      // build the DefaultParserRegistry
      val parserRegistry =
        this.parserStaging.asSequence().fold(defaultParserRegistry(this.classLoader)) { registry, (ext, parser) ->
          registry.register(ext, parser)
        }

      // other defaults
      val propertySources = when {
        defaultSources -> defaultPropertySources() + this.propertySourceStaging
        else -> this.propertySourceStaging
      }

      val preprocessors = when {
        defaultPreprocessors -> defaultPreprocessors() + this.preprocessorStaging
        else -> this.preprocessorStaging
      }

      val paramMappers = when {
        defaultParamMappers -> defaultParamMappers() + this.paramMapperStaging
        else -> this.paramMapperStaging
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

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A = loadConfigOrThrow(resources.toList())

  inline fun <reified A : Any> loadConfigOrThrow(resources: List<String>): A = loadConfig<A>(resources).returnOrThrow()

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be convered into the applicable types, then an this
   * function will throw.
   *
   * This function is intended to be used when you have registered all config files via the
   * builder's addPropertySource method.
   */
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
  inline fun <reified A : Any> loadConfig(resources: List<String>): ConfigResult<A> =
    ConfigSource.fromClasspathResources(resources.toList()).flatMap { loadConfig(A::class, it) }

  fun loadNodeOrThrow(resources: List<String>): Node =
    ConfigSource.fromClasspathResources(resources.toList()).flatMap { loadNode(it) }.returnOrThrow()

  fun loadNodeOrThrow(): Node = loadNode(emptyList()).returnOrThrow()

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
  fun loadNodeOrThrow(paths: List<Path>): Node =
    ConfigSource.fromPaths(paths.toList()).flatMap { loadNode(it) }.returnOrThrow()

  /**
   * Attempts to load config from the specified Paths and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg paths: Path): ConfigResult<A> = loadConfig(paths.toList())

  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class, emptyList())

  @JvmName("loadConfigFromPaths")
  inline fun <reified A : Any> loadConfig(paths: List<Path>): ConfigResult<A> {
    return ConfigSource.fromPaths(paths.toList()).flatMap { loadConfig(A::class, it) }
  }

  /**
   * Attempts to load config from the specified Files and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg files: File): A = loadConfigOrThrow(files.toList())

  @JvmName("loadConfigOrThrowFromFiles")
  inline fun <reified A : Any> loadConfigOrThrow(files: List<File>): A = loadConfig<A>(files).returnOrThrow()

  fun <A : Any> loadConfigOrThrow(klass: KClass<A>, inputs: List<ConfigSource>): A =
    loadConfig(klass, inputs).returnOrThrow()

  @JvmName("loadNodeOrThrowFromFiles")
  fun loadNodeOrThrow(files: List<File>): Node =
    ConfigSource.fromFiles(files.toList()).flatMap { loadNode(it) }.returnOrThrow()

  /**
   * Attempts to load config from the specified Files and returns
   * a [ConfigResult] with either the errors during load, or the successfully created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg files: File): ConfigResult<A> = loadConfig(files.toList())

  @JvmName("loadConfigFromFiles")
  inline fun <reified A : Any> loadConfig(files: List<File>): ConfigResult<A> {
    return ConfigSource.fromFiles(files.toList()).flatMap { loadConfig(A::class, it) }
  }

  fun <A : Any> loadConfig(klass: KClass<A>, inputs: List<ConfigSource>): ConfigResult<A> {
    // This is where the actual processing takes place. All other loadConfig or throw methods
    // ultimately end up in this method.
    require(klass.isData) { "Can only decode into data classes [was ${klass}]" }
    return if (decoderRegistry.size == 0)
      ConfigFailure.EmptyDecoderRegistry.invalid()
    else
      loadNode(inputs).flatMap { decode(klass, it) }
  }

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.getOrElse { failure ->
    val err = "Error loading config because:\n\n" + failure.description().indent(Constants.indent)
    onFailure.forEach { it(ConfigException(err)) }
    throw ConfigException(err)
  }

  private fun <A : Any> decode(kclass: KClass<A>, node: Node): ConfigResult<A> {
    return decoderRegistry.decoder(kclass).flatMap { decoder ->
      val context = DecoderContext(decoderRegistry, paramMappers, preprocessors, mode)
      val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
      decoder.decode(preprocessed, kclass.createType(), context)
    }
  }

  /**
   * Loads all property sources and combines them into a single node.
   */
  private fun loadNode(configs: List<ConfigSource>): ConfigResult<Node> {
    val srcs = propertySources + configs.map { ConfigFilePropertySource(it) }
    return srcs.map { it.node(PropertySourceContext(parserRegistry)) }.sequence()
      .map { it.takeUnless { it.isEmpty() }?.reduce { acc, b -> acc.merge(b) } ?: NullNode(Pos.NoPos)}
      .mapInvalid {
        val multipleFailures = ConfigFailure.MultipleFailures(it)
        multipleFailures
      }
  }
}

