@file:Suppress("unused")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.*
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.parsers.*
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.defaultPreprocessors
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class ConfigException(msg: String) : java.lang.RuntimeException(msg)

class ConfigLoader private constructor(
  private val decoderRegistry: DecoderRegistry,
  private val propertySources: List<PropertySource>,
  private val parserRegistry: ParserRegistry,
  private val preprocessors: List<Preprocessor>,
  private val paramMappers: List<ParameterMapper>
) {

  companion object {
    operator fun invoke(): ConfigLoader {
      return Builder().build()
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

    fun withClassLoader(classLoader: ClassLoader): Builder {
      if (this.classLoader !== classLoader) {
        this.classLoader = classLoader
      }
      return this
    }

    fun addDecoder(decoder: Decoder<*>): Builder {
      this.decoderStaging.add(decoder)
      return this
    }

    fun addDecoders(decoders: Iterable<Decoder<*>>): Builder {
      this.decoderStaging.addAll(decoders)
      return this
    }

    fun addFileExtensionMapping(ext: String, parser: Parser): Builder {
      this.parserStaging[ext] = parser
      return this
    }

    fun addFileExtensionMappins(map: Map<String, Parser>): Builder {
      map.forEach {
        val (ext, parser) = it
        this.parserStaging[ext] = parser
      }
      return this
    }

    fun addPropertySource(propertySource: PropertySource): Builder {
      this.propertySourceStaging.add(propertySource)
      return this
    }

    fun addPropertySources(propertySources: Iterable<PropertySource>): Builder {
      this.propertySourceStaging.addAll(propertySources)
      return this
    }

    fun addPreprocessor(preprocessor: Preprocessor): Builder {
      this.preprocessorStaging.add(preprocessor)
      return this
    }

    fun addPreprocessors(preprocessors: Iterable<Preprocessor>): Builder {
      this.preprocessorStaging.addAll(preprocessors)
      return this
    }

    fun addParameterMapper(paramMapper: ParameterMapper): Builder {
      this.paramMapperStaging.add(paramMapper)
      return this
    }

    fun addParameterMappers(paramMappers: Iterable<ParameterMapper>): Builder {
      this.paramMapperStaging.addAll(paramMappers)
      return this
    }

    fun build(): ConfigLoader {
      // build the DefaultDecoderRegistry
      val decoderRegistry = defaultDecoderRegistry(this.classLoader).apply {
        this@Builder.decoderStaging.forEach(this::register)
      }

      // build the DefaultParserRegistry
      val parserRegistry = defaultParserRegistry(this.classLoader).apply {
        this@Builder.parserStaging.forEach {
          val (ext, parser) = it
          this.register(ext, parser)
        }
      }

      // other defaults
      val propertySources = defaultPropertySources(parserRegistry)
      val preprocessors = defaultPreprocessors() + this.preprocessorStaging
      val paramMappers = defaultParamMappers() + this.paramMapperStaging

      return ConfigLoader(
        decoderRegistry = decoderRegistry,
        propertySources = propertySources,
        parserRegistry = parserRegistry,
        preprocessors = preprocessors,
        paramMappers = paramMappers
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

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.getOrElse {
    val err = "Error loading config because:\n\n" + it.description().indent(Constants.indent)
    throw ConfigException(err)
  }

  fun <A : Any> loadConfig(klass: KClass<A>, inputs: List<ConfigSource>): ConfigResult<A> {
    require(klass.isData) { "Can only decode into data classes [was ${klass}]" }
    return if (decoderRegistry.size == 0)
      ConfigFailure.EmptyDecoderRegistry.invalid()
    else
      loadNode(inputs).flatMap { decode(klass, it) }
  }

  private fun <A : Any> decode(kclass: KClass<A>, node: Node): ConfigResult<A> {
    return decoderRegistry.decoder(kclass).flatMap { decoder ->
      val context = DecoderContext(decoderRegistry, paramMappers, preprocessors)
      val preprocessed = context.preprocessors.fold(node) { acc, preprocessor -> preprocessor.process(acc) }
      decoder.decode(preprocessed, kclass.createType(), context)
    }
  }

  private fun loadNode(configs: List<ConfigSource>): ConfigResult<Node> {
    val srcs = propertySources + configs.map { ConfigFilePropertySource(it, parserRegistry) }
    return srcs.map { it.node() }.sequence()
      .map { it.reduce { acc, b -> acc.fallback(b) } }
      .mapInvalid { val multipleFailures = ConfigFailure.MultipleFailures(it)
        multipleFailures
      }
  }
}

