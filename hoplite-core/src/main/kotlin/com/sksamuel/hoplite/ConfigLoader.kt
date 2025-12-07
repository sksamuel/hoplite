@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.internal.ConfigParser
import com.sksamuel.hoplite.internal.DecodeMode
import com.sksamuel.hoplite.transformer.NodeTransformer
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import kotlin.reflect.KClass

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader(
  val decoderRegistry: DecoderRegistry,
  val propertySources: List<PropertySource>,
  val parserRegistry: ParserRegistry,
  val preprocessors: List<Preprocessor>,
  val nodeTransformers: List<NodeTransformer>,
  val paramMappers: List<ParameterMapper>,
  val onFailure: List<(Throwable) -> Unit> = emptyList(),
  val decodeMode: DecodeMode = DecodeMode.Lenient,
  val useReport: Boolean = false,
  val allowEmptyTree: Boolean, // if true then we allow the cascaded tree to be empty
  val allowUnresolvedSubstitutions: Boolean,
  val classLoader: ClassLoader? = null, // if null, then the current context thread loader
  val preprocessingIterations: Int = 1,
  val cascadeMode: CascadeMode = CascadeMode.Merge,
  val secretsPolicy: SecretsPolicy? = null,
  val environment: Environment? = null,
  val obfuscator: Obfuscator? = null,
  val reportPrintFn: Print = { println(it) },
  val flattenArraysToString: Boolean = false,
  val resolvers: List<Resolver> = emptyList(),
  val sealedTypeDiscriminatorField: String? = null,
  val allowNullOverride: Boolean = false,
  val resolveTypesCaseInsensitive: Boolean = false,
  val contextResolverMode: ContextResolverMode = ContextResolverMode.ErrorOnUnresolved,
) {

  init {
    if (sealedTypeDiscriminatorField == null) {
      reportPrintFn.invoke(
        "Hoplite is configured to infer which sealed type to choose by inspecting the config values at runtime. " +
          "This behaviour is now deprecated in favour of explicitly specifying the type through a discriminator field. " +
          "In 3.0 this new behavior will become the default. " +
          "To enable this behavior now (and disable this warning), invoke withExplicitSealedTypes() on the ConfigLoaderBuilder."
      )
    }
  }

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
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  inline fun <reified A : Any> loadConfigOrThrow(prefix: String? = null): A =
    loadConfigOrThrow(emptyList(), prefix = prefix)

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns the successfully created instance A, or throws an error.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  inline fun <reified A : Any> loadConfigOrThrow(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
    prefix: String? = null,
  ): A = loadConfig<A>(resourceOrFiles, classpathResourceLoader, prefix).returnOrThrow()

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then this
   * function will throw.
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  fun <A : Any> loadConfigOrThrow(klass: KClass<A>, inputs: List<ConfigSource>, prefix: String? = null): A =
    loadConfig(klass, inputs, emptyList(), prefix).returnOrThrow()

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  inline fun <reified A : Any> loadConfig(
    vararg resourceOrFiles: String,
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
    prefix: String? = null,
  ): ConfigResult<A> = loadConfig(resourceOrFiles.toList(), classpathResourceLoader, prefix)

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  inline fun <reified A : Any> loadConfig(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
    prefix: String? = null,
  ): ConfigResult<A> = loadConfig(A::class, emptyList(), resourceOrFiles, prefix, classpathResourceLoader)

  /**
   * Attempts to load config from the specified resources either on the class path or as files on the
   * file system, and returns a [ConfigResult] with either the errors during load, or the successfully
   * created instance A.
   *
   * This function implements fallback, such that the first resource is scanned first, and the second
   * resource is scanned if the first does not contain a given path, and so on.
   *
   * A subtree of the loaded config can be bound using the `prefix` parameter. However, consider instead
   * using the [configBinder] method to load the configuration just once, and then use the resulting
   * [ConfigBinder] to bind prefixes to types via [ConfigBinder.bindOrThrow]. Calling this method multiple
   * times with the same sources but different prefix values will unnecessarily load the same config multiple
   * times.
   */
  inline fun <reified A : Any> loadConfig(
    classpathResourceLoader: ClasspathResourceLoader = ConfigSource.Companion::class.java.toClasspathResourceLoader(),
    prefix: String? = null,
  ): ConfigResult<A> = loadConfig(emptyList(), classpathResourceLoader, prefix)

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then this
   * function will return an invalid [ConfigFailure].
   */
  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class, emptyList(), emptyList(), null)

  /**
   * Create a [ConfigBinder] which can be used to bind instances of config classes using the same parsed
   * configuration.
   *
   * It would be common for a system to create a [ConfigBinder] once and inject that [ConfigBinder] into independent
   * modules that each require their own config binding. See [ConfigBinder.bindOrThrow].
   */
  fun configBinder(
    resourceOrFiles: List<String> = emptyList(),
    configSources: List<ConfigSource> = emptyList(),
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader()
  ): ConfigBinder {
    val configParser = createConfigParser(classpathResourceLoader, resourceOrFiles, configSources)
    return ConfigBinder(configParser, environment)
  }

  @PublishedApi
  internal fun <A : Any> loadConfig(
    kclass: KClass<A>,
    configSources: List<ConfigSource>,
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ): ConfigResult<A> {
    return loadConfig(kclass, configSources, resourceOrFiles, null, classpathResourceLoader)
  }

  // This is where the actual processing takes place for marshalled config.
  // All other loadConfig or loadConfigOrThrow methods ultimately end up in this method.
  fun <A : Any> bindConfig(
    parser: ConfigParser,
    kclass: KClass<A>,
    prefix: String?,
  ): ConfigResult<A> {
    require(kclass.isData) { "Can only decode into data classes [was ${kclass}]" }
    return parser.decode(kclass, environment, prefix)
  }


  // This is where the actual processing takes place for marshalled config.
  // All other loadConfig or loadConfigOrThrow methods ultimately end up in this method.
  @PublishedApi
  internal fun <A : Any> loadConfig(
    kclass: KClass<A>,
    configSources: List<ConfigSource>,
    resourceOrFiles: List<String>,
    prefix: String?,
    classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader()
  ): ConfigResult<A> {
    require(kclass.isData) { "Can only decode into data classes [was ${kclass}]" }
    val configParser = createConfigParser(
      resourceOrFiles = resourceOrFiles,
      configSources = configSources,
      classpathResourceLoader = classpathResourceLoader,
    )
    return configParser.decode(kclass, environment, prefix)
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
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader()
  ): Node = loadNode(resourceOrFiles, emptyList(), classpathResourceLoader).returnOrThrow()

  fun loadNode(vararg resourceOrFiles: String): ConfigResult<Node> = loadNode(resourceOrFiles.toList())

  fun loadNode(
    resourceOrFiles: List<String>,
    configSources: List<ConfigSource> = emptyList(),
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader()
  ): ConfigResult<Node> {
    val configParser = createConfigParser(
      resourceOrFiles = resourceOrFiles,
      configSources = configSources,
      classpathResourceLoader = classpathResourceLoader,
    )
    return configParser.load()
  }

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = returnOrThrow(onFailure)

  private fun createConfigParser(
    classpathResourceLoader: ClasspathResourceLoader,
    resourceOrFiles: List<String>,
    configSources: List<ConfigSource>,
  ): ConfigParser = ConfigParser(
    classpathResourceLoader = classpathResourceLoader,
    parserRegistry = parserRegistry,
    allowEmptyTree = allowEmptyTree,
    allowNullOverride = allowNullOverride,
    cascadeMode = cascadeMode,
    preprocessors = preprocessors,
    preprocessingIterations = preprocessingIterations,
    nodeTransformers = nodeTransformers,
    resolvers = resolvers,
    decoderRegistry = decoderRegistry,
    paramMappers = paramMappers,
    flattenArraysToString = flattenArraysToString,
    resolveTypesCaseInsensitive = resolveTypesCaseInsensitive,
    allowUnresolvedSubstitutions = allowUnresolvedSubstitutions,
    secretsPolicy = secretsPolicy,
    decodeMode = decodeMode,
    useReport = useReport,
    obfuscator = obfuscator ?: PrefixObfuscator(3),
    reportPrintFn = reportPrintFn,
    environment = environment,
    sealedTypeDiscriminatorField = sealedTypeDiscriminatorField,
    contextResolverMode = contextResolverMode,
    resourceOrFiles = resourceOrFiles,
    propertySources = propertySources,
    configSources = configSources,
  )
}

@Deprecated("Moved package. Use com.sksamuel.hoplite.sources.MapPropertySource")
typealias MapPropertySource = com.sksamuel.hoplite.sources.MapPropertySource

@PublishedApi
internal fun <A : Any> ConfigResult<A>.returnOrThrow(onFailure: List<(Throwable) -> Unit>): A = this.getOrElse { failure ->
  val err = "Error loading config because:\n\n" + failure.description().indent(Constants.indent)
  onFailure.forEach { it(ConfigException(err)) }
  throw ConfigException(err)
}
