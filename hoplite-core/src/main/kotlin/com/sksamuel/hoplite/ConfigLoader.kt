@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.UnresolvedSubstitutionChecker
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretStrength
import com.sksamuel.hoplite.secrets.SecretStrengthAnalyzer
import com.sksamuel.hoplite.secrets.SecretsPolicy
import kotlin.reflect.KClass

class ConfigException(msg: String, val t: Throwable? = null) : java.lang.RuntimeException(msg, t)

class ConfigLoader(
  val decoderRegistry: DecoderRegistry,
  val propertySources: List<PropertySource>,
  val parserRegistry: ParserRegistry,
  val preprocessors: List<Preprocessor>,
  val paramMappers: List<ParameterMapper>,
  val onFailure: List<(Throwable) -> Unit> = emptyList(),
  val mode: DecodeMode = DecodeMode.Lenient,
  val useReport: Boolean = false,
  val allowEmptyTree: Boolean, // if true then we allow config files to be empty
  val allowUnresolvedSubstitutions: Boolean,
  val classLoader: ClassLoader? = null, // if null, then the current context thread loader
  val preprocessingIterations: Int = 1,
  val cascadeMode: CascadeMode = CascadeMode.Merge,
  val secretsPolicy: SecretsPolicy? = null,
  val secretStrengthAnalyzer: MutableMap<Environment?, SecretStrengthAnalyzer> = mutableMapOf(),
  val environment: Environment? = null,
  val obfuscator: Obfuscator? = null,
  val failOnWeakSecrets: Set<Environment?> = emptySet(),
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
    loadConfig(klass, inputs).returnOrThrow()

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
  ): ConfigResult<A> =
    ConfigSource
      .fromResourcesOrFiles(resourceOrFiles.toList(), classpathResourceLoader)
      .flatMap { loadConfig(A::class, it) }

  /**
   * Attempts to load config from the registered property sources marshalled as an instance of A.
   * If any properties are missing, or cannot be converted into the applicable types, then this
   * function will return an invalid [ConfigFailure].
   */
  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig(A::class, emptyList())

  fun <A : Any> loadConfig(klass: KClass<A>, configSources: List<ConfigSource>): ConfigResult<A> {
    // This is where the actual processing takes place for marshalled config.
    // All other loadConfig or throw methods ultimately end up in this method.
    require(klass.isData) { "Can only decode into data classes [was ${klass}]" }

    if (decoderRegistry.size == 0)
      return ConfigFailure.EmptyDecoderRegistry.invalid()

    return NodeParser(parserRegistry, allowEmptyTree, cascadeMode)
      .parseNode(propertySources, configSources)
      .flatMap { (sources, node) ->
        Preprocessing(preprocessors, preprocessingIterations).preprocess(node)
          .flatMap { if (allowUnresolvedSubstitutions) it.valid() else UnresolvedSubstitutionChecker.process(it) }
          .flatMap { preprocessed ->

            val context = DecoderContext(
              decoders = decoderRegistry,
              paramMappers = paramMappers,
            )

            val decoded = decode(klass, preprocessed, context)
            val state = createDecodingState(preprocessed, context, secretsPolicy, secretStrengthAnalyzer[environment])

            // always do report regardless of decoder result
            if (useReport) {
              Reporter({ println(it) }, obfuscator ?: PrefixObfuscator(3), environment).printReport(sources, state)
            }

            val weak = state.states.mapNotNull {
              when (it.secretStrength) {
                is SecretStrength.Weak -> ConfigFailure.WeakSecret(it.node.path, it.secretStrength)
                else -> null
              }
            }

            // if we have a weak secret, then die
            if (failOnWeakSecrets.contains(environment) && weak.isNotEmpty()) {
              ConfigFailure.MultipleFailures(NonEmptyList.unsafe(weak)).invalid()
            } else decoded
          }
      }
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
  ): Node = loadNode(resourceOrFiles, classpathResourceLoader).returnOrThrow()

  fun loadNode(vararg resourceOrFiles: String): ConfigResult<Node> = loadNode(resourceOrFiles.toList())

  fun loadNode(
    resourceOrFiles: List<String>,
    classpathResourceLoader: ClasspathResourceLoader = ConfigLoader::class.java.toClasspathResourceLoader(),
  ): ConfigResult<Node> = ConfigSource
    .fromResourcesOrFiles(resourceOrFiles.toList(), classpathResourceLoader)
    .flatMap { NodeParser(parserRegistry, allowEmptyTree, cascadeMode).parseNode(propertySources, it) }
    .flatMap { Preprocessing(preprocessors, preprocessingIterations).preprocess(it.node) }

  private fun <A : Any> decode(kclass: KClass<A>, node: Node, context: DecoderContext): ConfigResult<A> {
    val decoding = Decoding(decoderRegistry, secretsPolicy, secretStrengthAnalyzer[environment])
    return decoding.decode(kclass, node, mode, context)
  }

  @PublishedApi
  internal fun <A : Any> ConfigResult<A>.returnOrThrow(): A = this.getOrElse { failure ->
    val err = "Error loading config because:\n\n" + failure.description().indent(Constants.indent)
    onFailure.forEach { it(ConfigException(err)) }
    throw ConfigException(err)
  }
}

