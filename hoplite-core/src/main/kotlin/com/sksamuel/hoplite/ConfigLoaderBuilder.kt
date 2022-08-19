package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DefaultDecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.parsers.DefaultParserRegistry
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.preprocessor.EnvOrSystemPropertyPreprocessor
import com.sksamuel.hoplite.preprocessor.LookupPreprocessor
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.RandomPreprocessor
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.secrets.AllStringNodesSecretsPolicy
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.sources.EnvironmentVariableOverridePropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import com.sksamuel.hoplite.sources.UserSettingsPropertySource
import com.sksamuel.hoplite.sources.XdgConfigPropertySource
import java.util.ServiceLoader

class ConfigLoaderBuilder private constructor() {

  private val failureCallbacks = mutableListOf<(Throwable) -> Unit>()

  // this is the default class loader that ServiceLoader::load(Class<T>)
  // gets before delegating to ServiceLoader::load(Class<T>, ClassLoader)
  private var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

  private var decodeMode: DecodeMode = DecodeMode.Lenient
  private var cascadeMode: CascadeMode = CascadeMode.Merge
  private var allowEmptyTree = false
  private var allowUnresolvedSubstitutions = false

  private val propertySources = mutableListOf<PropertySource>()
  private val preprocessors = mutableListOf<Preprocessor>()
  private val paramMappers = mutableListOf<ParameterMapper>()
  private val parsers = mutableMapOf<String, Parser>()
  private val decoders = mutableListOf<Decoder<*>>()

  private var useReport: Boolean = false
  private var secretsPolicy: SecretsPolicy = AllStringNodesSecretsPolicy
  private var obfuscator: Obfuscator = PrefixObfuscator(3)
  private var preprocessingIterations: Int = 1

  private var environment: Environment? = null
  private var flattenArraysToString: Boolean = false

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

    fun empty(block: ConfigLoaderBuilder.() -> Unit): ConfigLoaderBuilder {
      val builder = ConfigLoaderBuilder()
      builder.block()
      return builder
    }
  }

  fun withClassLoader(classLoader: ClassLoader): ConfigLoaderBuilder = apply {
    this.classLoader = classLoader
  }

  /**
   * Sets the current environment, eg prod or dev.
   */
  @ExperimentalHoplite
  fun withEnvironment(environment: Environment) = apply { this.environment = environment }

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

  fun addParser(ext: String, parser: Parser) = addFileExtensionMapping(ext, parser)
  fun addParsers(map: Map<String, Parser>) = addFileExtensionMappings(map)

  fun addFileExtensionMapping(ext: String, parser: Parser): ConfigLoaderBuilder = apply {
    this.parsers[ext] = parser
  }

  fun addFileExtensionMappings(map: Map<String, Parser>): ConfigLoaderBuilder = apply {
    map.forEach {
      val (ext, parser) = it
      this.parsers[ext] = parser
    }
  }

  fun addDefaultParsers() = apply {
    ServiceLoader.load(Parser::class.java, classLoader).toList().forEach { parser ->
      parser.defaultFileExtensions().forEach { ext -> addFileExtensionMapping(ext, parser) }
    }
  }

  fun withPreprocessingIterations(iterations: Int): ConfigLoaderBuilder = apply {
    preprocessingIterations = iterations
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

  @ExperimentalHoplite
  fun flattenArraysToString(): ConfigLoaderBuilder = apply { this.flattenArraysToString = true }

  fun addDefaults(): ConfigLoaderBuilder {
    return addDefaultDecoders()
      .addDefaultParsers()
      .addDefaultPreprocessors()
      .addDefaultParamMappers()
      .addDefaultPropertySources()
  }

  /**
   * When [DecodeMode.Strict] is enabled, if any config values from property sources are unused,
   * the config loader will error. This enables you to easily find stale config and fix it.
   */
  fun strict(): ConfigLoaderBuilder = withDecodeMode(DecodeMode.Strict)
  fun lenient(): ConfigLoaderBuilder = withDecodeMode(DecodeMode.Lenient)

  fun withDecodeMode(mode: DecodeMode) = apply { this.decodeMode = mode }

  @ExperimentalHoplite
  fun withCascadeMode(cascadeMode: CascadeMode) = apply { this.cascadeMode = cascadeMode }

  /**
   * When enabled, allows a config loader to continue even if all the property sources provide no config.
   */
  fun allowEmptyTree(): ConfigLoaderBuilder = apply {
    allowEmptyTree = true
  }

  /**
   * When enabled, allows placeholder substitutions like ${foo} not to cause an error if they are not resolvable.
   */
  fun allowUnresolvedSubstitutions(): ConfigLoaderBuilder = apply {
    allowUnresolvedSubstitutions = true
  }

  /**
   * Enables a report on all config keys, their values, and which were used or unused.
   * Note, to avoid printing passwords or other secrets, wrap those values by using `Masked` or `Secret`
   * as the target type instead of String. Values coming from a secrets manager preprocessor will automatically
   * be marked to secrets.
   *
   * The report will be printed to standard out.
   */
  fun withReport() = apply { this.useReport = true }

  @Deprecated("use withReport()", ReplaceWith("withReport()"))
  fun report() = withReport()

  @Deprecated("Use correct spelling", ReplaceWith("withObfusctator(obfuscator)"))
  fun withObfusctator(obfuscator: Obfuscator): ConfigLoaderBuilder = withObfuscator(obfuscator)
  fun withObfuscator(obfuscator: Obfuscator): ConfigLoaderBuilder = apply { this.obfuscator = obfuscator }

  @ExperimentalHoplite
  fun withSecretsPolicy(secretsPolicy: SecretsPolicy) = apply { this.secretsPolicy = secretsPolicy }

  /**
   * Enables a report on all config keys, their values, and which were used or unused.
   * Note, to avoid printing passwords or other secrets, wrap those values by using `Masked` or `Secret`
   * as the target type instead of String.
   *
   * The report will be printed using the given function.
   */
  @Deprecated(
    "use withReport(). Passing in a reporter no longer has any effect. Specify secrets policy and obfuscator directly on this builder.",
    ReplaceWith("withReport()"),
    level = DeprecationLevel.ERROR,
  )
  fun withReport(reporter: Reporter) = apply { useReport = true }

  @Deprecated(
    "use withReport(). Passing in a reporter no longer has any effect. Specify secrets policy and obfuscator directly on this builder.",
    ReplaceWith("withReport()"),
    level = DeprecationLevel.ERROR,
  )
  fun report(reporter: Reporter) = apply { useReport = true }

  fun build(): ConfigLoader {
    return ConfigLoader(
      decoderRegistry = DefaultDecoderRegistry(decoders),
      propertySources = propertySources.toList(),
      parserRegistry = DefaultParserRegistry(parsers),
      preprocessors = preprocessors.toList(),
      paramMappers = paramMappers.toList(),
      onFailure = failureCallbacks.toList(),
      mode = decodeMode,
      useReport = useReport,
      allowEmptyTree = allowEmptyTree,
      allowUnresolvedSubstitutions = allowUnresolvedSubstitutions,
      preprocessingIterations = preprocessingIterations,
      cascadeMode = cascadeMode,
      secretsPolicy = secretsPolicy,
      environment = environment,
      obfuscator = obfuscator,
      flattenArraysToString = flattenArraysToString,
    )
  }
}

fun defaultPropertySources(): List<PropertySource> = listOfNotNull(
  EnvironmentVariableOverridePropertySource(true),
  SystemPropertiesPropertySource,
  UserSettingsPropertySource,
  XdgConfigPropertySource,
)

fun defaultPreprocessors() = listOf(
  EnvOrSystemPropertyPreprocessor,
  RandomPreprocessor,
  LookupPreprocessor
)

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  SnakeCaseParamMapper,
  KebabCaseParamMapper,
  AliasAnnotationParamMapper,
)

val defaultDecoders = listOf(
  com.sksamuel.hoplite.decoder.UUIDDecoder(),
  com.sksamuel.hoplite.decoder.LocalDateDecoder(),
  com.sksamuel.hoplite.decoder.LocalDateTimeDecoder(),
  com.sksamuel.hoplite.decoder.LocalTimeDecoder(),
  com.sksamuel.hoplite.decoder.InstantDecoder(),
  com.sksamuel.hoplite.decoder.DurationDecoder(),
  com.sksamuel.hoplite.decoder.KotlinDurationDecoder(),
  com.sksamuel.hoplite.decoder.PeriodDecoder(),
  com.sksamuel.hoplite.decoder.SqlTimestampDecoder(),
  com.sksamuel.hoplite.decoder.EnumDecoder(),
  com.sksamuel.hoplite.decoder.StringDecoder(),
  com.sksamuel.hoplite.decoder.IntDecoder(),
  com.sksamuel.hoplite.decoder.FloatDecoder(),
  com.sksamuel.hoplite.decoder.DoubleDecoder(),
  com.sksamuel.hoplite.decoder.ShortDecoder(),
  com.sksamuel.hoplite.decoder.ByteDecoder(),
  com.sksamuel.hoplite.decoder.BooleanDecoder(),
  com.sksamuel.hoplite.decoder.RegexDecoder(),
  com.sksamuel.hoplite.decoder.LongDecoder(),
  com.sksamuel.hoplite.decoder.ListDecoder(),
  com.sksamuel.hoplite.decoder.SortedSetDecoder(),
  com.sksamuel.hoplite.decoder.SetDecoder(),
  com.sksamuel.hoplite.decoder.LinkedHashMapDecoder(),
  com.sksamuel.hoplite.decoder.MapDecoder(),
  com.sksamuel.hoplite.decoder.FileDecoder(),
  com.sksamuel.hoplite.decoder.SizeInBytesDecoder(),
  com.sksamuel.hoplite.decoder.PathDecoder(),
  com.sksamuel.hoplite.decoder.BigIntegerDecoder(),
  com.sksamuel.hoplite.decoder.BigDecimalDecoder(),
  com.sksamuel.hoplite.decoder.PrincipalDecoder(),
  com.sksamuel.hoplite.decoder.InetAddressDecoder(),
  com.sksamuel.hoplite.decoder.URLDecoder(),
  com.sksamuel.hoplite.decoder.KClassDecoder(),
  com.sksamuel.hoplite.decoder.URIDecoder(),
  com.sksamuel.hoplite.decoder.SecretDecoder(),
  com.sksamuel.hoplite.decoder.SecretDecoder(),
  com.sksamuel.hoplite.decoder.TripleDecoder(),
  com.sksamuel.hoplite.decoder.PairDecoder(),
  com.sksamuel.hoplite.decoder.YearDecoder(),
  com.sksamuel.hoplite.decoder.MonthDayDecoder(),
  com.sksamuel.hoplite.decoder.JavaUtilDateDecoder(),
  com.sksamuel.hoplite.decoder.IntRangeDecoder(),
  com.sksamuel.hoplite.decoder.LongRangeDecoder(),
  com.sksamuel.hoplite.decoder.CharRangeDecoder(),
  com.sksamuel.hoplite.decoder.YearMonthDecoder(),
  com.sksamuel.hoplite.decoder.MinutesDecoder(),
  com.sksamuel.hoplite.decoder.SecondsDecoder(),
  com.sksamuel.hoplite.decoder.InlineClassDecoder(),
  com.sksamuel.hoplite.decoder.SealedClassDecoder(),
  com.sksamuel.hoplite.decoder.DataClassDecoder(),
)
