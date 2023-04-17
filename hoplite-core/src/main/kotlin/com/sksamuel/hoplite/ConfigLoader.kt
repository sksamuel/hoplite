@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.internal.ConfigParser
import com.sksamuel.hoplite.internal.DecodeMode
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.secrets.StrictObfuscator
import kotlin.reflect.KClass

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader(
  val decoderRegistry: DecoderRegistry,
  val propertySources: List<PropertySource>,
  val parserRegistry: ParserRegistry,
  val preprocessors: List<Preprocessor>,
  val paramMappers: List<ParameterMapper>,
  val onFailure: List<(Throwable) -> Unit> = emptyList(),
  val decodeMode: DecodeMode = DecodeMode.Lenient,
  val useReport: Boolean = false,
  val allowEmptyTree: Boolean, // if true then we allow config files to be empty
  val allowUnresolvedSubstitutions: Boolean,
  val classLoader: ClassLoader? = null, // if null, then the current context thread loader
  val preprocessingIterations: Int = 1,
  val cascadeMode: CascadeMode = CascadeMode.Merge,
  val secretsPolicy: SecretsPolicy? = null,
  val environment: Environment? = null,
  val obfuscator: Obfuscator? = null,
  val reportPrintFn: Print? = null,
  val flattenArraysToString: Boolean = false,
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
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns the successfully created instance A, or throws an error.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(vararg resourceOrFiles: String): A =
    loadConfigOrThrow(resourceOrFiles.toList())

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns the successfully created instance A, or throws an error.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfigOrThrow(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): A = loadConfig<A>(resourceOrFiles, classpathResourceLoader).returnOrThrow()

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then this
   * function will throw.
   */
  fun <A : Any> loadConfigOrThrow(klass: KClass<A>, inputs: List<ConfigSource>): A =
    loadConfig(klass, inputs, emptyList()).returnOrThrow()

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(
    vararg resourceOrFiles: String,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> = loadConfig(resourceOrFiles.toList(), classpathResourceLoader)

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  inline fun <reified A : Any> loadConfig(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> = loadConfig(A::class, emptyList(), resourceOrFiles, classpathResourceLoader)

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then this
   * function will return an invalid [ConfigFailure].
   */
  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class, emptyList(), emptyList())

  // This is where the actual processing takes place for marshalled config.
  // All other loadConfig or loadConfigOrThrow methods ultimately end up in this method.
  @PublishedApi
  internal fun <A : Any> loadConfig(
    kclass: KClass<A>,
    configSources: List<ConfigSource>,
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> {
    require(kclass.isData) { "Can only decode into data classes [was ${kclass}]" }
    return ConfigParser(
      classpathResourceLoader = classpathResourceLoader,
      parserRegistry = parserRegistry,
      allowEmptyTree = allowEmptyTree,
      cascadeMode = cascadeMode,
      preprocessors = preprocessors,
      preprocessingIterations = preprocessingIterations,
      decoderRegistry = decoderRegistry,
      paramMappers = paramMappers,
      flattenArraysToString = flattenArraysToString,
      allowUnresolvedSubstitutions = allowUnresolvedSubstitutions,
      secretsPolicy = secretsPolicy,
      decodeMode = decodeMode,
      useReport = useReport,
      obfuscator = obfuscator ?: PrefixObfuscator(3),
      reportPrintFn = reportPrintFn ?: { println(it) },
      environment = environment,
    ).decode(kclass, environment, resourceOrFiles, propertySources, configSources)
  }

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   */
  fun loadNodeOrThrow(vararg resourceOrFiles: String): Node = loadNodeOrThrow(resourceOrFiles.toList())

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
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader(),
  ): Node = loadNode(resourceOrFiles, emptyList(), classpathResourceLoader).returnOrThrow()

  fun loadNode(vararg resourceOrFiles: String): ConfigResult<Node> = loadNode(resourceOrFiles.toList())

  fun loadNode(
    resourceOrFiles: List<String>,
    configSources: List<ConfigSource> = emptyList(),
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader(),
  ): ConfigResult<Node> {
    return ConfigParser(
      classpathResourceLoader = classpathResourceLoader,
      parserRegistry = parserRegistry,
      allowEmptyTree = allowEmptyTree,
      cascadeMode = cascadeMode,
      preprocessors = preprocessors,
      preprocessingIterations = preprocessingIterations,
      decoderRegistry = decoderRegistry,
      paramMappers = paramMappers,
      flattenArraysToString = false, // not needed to load nodes
      allowUnresolvedSubstitutions = allowUnresolvedSubstitutions,
      secretsPolicy = null, // not used when loading nodes
      decodeMode = DecodeMode.Lenient,  // not used when loading nodes
      useReport = false,  // not used when loading nodes
      obfuscator = StrictObfuscator("*"),  // not used when loading nodes
      reportPrintFn = reportPrintFn ?: { }, // not used when loading nodes
      environment = environment,
    ).load(resourceOrFiles, propertySources, configSources)
  }

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.getOrElse { failure ->
    val err = "Error loading config because:\n\n" + failure.description().indent(Constants.indent)
    onFailure.forEach { it(ConfigException(err)) }
    throw ConfigException(err)
  }
}

@Deprecated("Moved package. Use com.sksamuel.hoplite.sources.MapPropertySource")
typealias MapPropertySource = com.sksamuel.hoplite.sources.MapPropertySource

