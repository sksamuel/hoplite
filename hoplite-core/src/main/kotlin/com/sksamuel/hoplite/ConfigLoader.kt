@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import kotlin.reflect.KClass

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader constructor(
  val decoderRegistry: DecoderRegistry,
  val propertySources: List<PropertySource>,
  val parserRegistry: ParserRegistry,
  val preprocessors: List<Preprocessor>,
  val paramMappers: List<ParameterMapper>,
  val onFailure: List<(Throwable) -> Unit> = emptyList(),
  val mode: DecodeMode = DecodeMode.Lenient,
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
    vararg resourceOrFiles: String
  ): A = loadConfigOrThrow(resourceOrFiles.toList())

  /**
   * Attempts to load config from the specified resources on the class path and returns
   * an instance of <A> if the values can be appropriately converted.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(
    resourceOrFile: String,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): A = loadConfigOrThrow(listOf(resourceOrFile), classpathResourceLoader)

  inline fun <reified A : Any> loadConfigOrThrow(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): A = loadConfig<A>(resourceOrFiles, classpathResourceLoader).returnOrThrow()

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> = loadConfig(resources.toList())

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then an this
   * function will throw.
   *
   * This function is intended to be used when you have registered all config files via the
   * builder's addPropertySource method.
   */
  fun loadNodeOrThrow(): Node = loadNode(emptyList()).returnOrThrow()

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  @JvmName("loadConfigFromResources")
  inline fun <reified A : Any> loadConfig(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> =
    ConfigSource
      .fromResourcesOrFiles(resourceOrFiles.toList(), classpathResourceLoader)
      .flatMap { loadConfig(A::class, it) }

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then an this
   * function will throw.
   *
   * This function is intended to be used when you have registered all config files via the
   * builder's addPropertySource method.
   */
  inline fun <reified A : Any> loadConfigOrThrow(): A = loadConfig(A::class, emptyList()).returnOrThrow()

  fun loadNodeOrThrow(
    vararg resourceOrFiles: String,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): Node = loadNodeOrThrow(resourceOrFiles.toList(), classpathResourceLoader)

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  fun loadNodeOrThrow(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): Node =
    ConfigSource
      .fromResourcesOrFiles(resourceOrFiles.toList(), classpathResourceLoader)
      .flatMap { loadNode(it) }
      .returnOrThrow()

  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class, emptyList())

  fun <A : Any> loadConfigOrThrow(klass: KClass<A>, inputs: List<ConfigSource>): A =
    loadConfig(klass, inputs).returnOrThrow()

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
    val decoding = Decoding(decoderRegistry, paramMappers, preprocessors)
    return decoding.decode(kclass, node, mode)
  }

  /**
   * Loads all property sources and combines them into a single node.
   */
  private fun loadNode(configs: List<ConfigSource>): ConfigResult<Node> {
    val srcs = propertySources + configs.map { ConfigFilePropertySource(it) }
    if (srcs.isEmpty()) return ConfigFailure.NoSources.invalid()
    return srcs.map { it.node(PropertySourceContext(parserRegistry)) }.sequence().map { nodes ->
      // nodes cannot be empty, as srcs is not empty, and if any of them errored, we would not be in this map block
      nodes.reduce { a, b -> a.merge(b) }
    }.mapInvalid {
      val multipleFailures = ConfigFailure.MultipleFailures(it)
      multipleFailures
    }
  }
}

