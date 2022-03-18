@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.UnresolvedSubstitutionChecker
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

    /**
     * Returns a [ConfigLoader] with default options applied.
     */
    operator fun invoke(): ConfigLoader = ConfigLoaderBuilder.default().build()

    /**
     * Returns a [ConfigLoaderBuilder] with default options applied, which can be further customized.
     */
    fun builder(): ConfigLoaderBuilder = ConfigLoaderBuilder.default()

    /**
     * Returns a [ConfigLoader] with default options with the given [block] applied.
     *
     * Eg,
     *
     * val loader = ConfigLoader {
     *   addDecoder(MyDecoder)
     *   addPropertySource(propSource)
     * }
     *
     */
    inline operator fun invoke(block: ConfigLoaderBuilder.() -> Unit): ConfigLoader {
      return ConfigLoaderBuilder.default().apply(block).build()
    }
  }

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(
    vararg resources: String
  ): A = loadConfigOrThrow(resources.toList())

  inline fun <reified A : Any> loadConfigOrThrow(
    resource: String,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): A = loadConfigOrThrow(listOf(resource), classpathResourceLoader)

  inline fun <reified A : Any> loadConfigOrThrow(
    resources: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): A = loadConfig<A>(resources, classpathResourceLoader).returnOrThrow()

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then an this
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
  inline fun <reified A : Any> loadConfig(
    resources: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> =
    ConfigSource.fromClasspathResources(resources.toList(), classpathResourceLoader)
      .flatMap { loadConfig(A::class, it) }

  fun loadNodeOrThrow(
    resources: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): Node =
    ConfigSource.fromClasspathResources(resources.toList(), classpathResourceLoader)
      .flatMap { loadNode(it) }
      .returnOrThrow()

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
      val errors = UnresolvedSubstitutionChecker.process(preprocessed)
      if (errors.isNotEmpty())
        ConfigFailure.MultipleFailures(NonEmptyList(errors)).invalid()
      else
        decoder.decode(preprocessed, kclass.createType(), context)
    }
  }

  /**
   * Loads all property sources and combines them into a single node.
   */
  private fun loadNode(configs: List<ConfigSource>): ConfigResult<Node> {
    val srcs = propertySources + configs.map { ConfigFilePropertySource(it) }
    return srcs.map { it.node(PropertySourceContext(parserRegistry)) }.sequence()
      .map { it.takeUnless { it.isEmpty() }?.reduce { acc, b -> acc.merge(b) } ?: NullNode(Pos.NoPos) }
      .mapInvalid {
        val multipleFailures = ConfigFailure.MultipleFailures(it)
        multipleFailures
      }
  }
}

