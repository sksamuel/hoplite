package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.DefaultDecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.internal.DecodeMode
import com.sksamuel.hoplite.transformer.NodeTransformer
import com.sksamuel.hoplite.parsers.DefaultParserRegistry
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.preprocessor.EnvOrSystemPropertyPreprocessor
import com.sksamuel.hoplite.preprocessor.LookupPreprocessor
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.preprocessor.RandomPreprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import com.sksamuel.hoplite.resolver.context.EnvVarContextResolver
import com.sksamuel.hoplite.resolver.context.HopliteContextResolver
import com.sksamuel.hoplite.resolver.context.ManifestContextResolver
import com.sksamuel.hoplite.resolver.context.RandomContextResolver
import com.sksamuel.hoplite.resolver.context.ReferenceContextResolver
import com.sksamuel.hoplite.resolver.context.SystemContextResolver
import com.sksamuel.hoplite.resolver.context.SystemPropertyContextResolver
import com.sksamuel.hoplite.secrets.AllStringNodesSecretsPolicy
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.PrefixObfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import com.sksamuel.hoplite.sources.UserSettingsPropertySource
import com.sksamuel.hoplite.sources.XdgConfigPropertySource
import com.sksamuel.hoplite.transformer.PathNormalizer
import java.util.ServiceLoader

class ConfigLoaderBuilder private constructor() {

  private val failureCallbacks = mutableListOf<(Throwable) -> Unit>()

  // this is the default class loader that ServiceLoader::load(Class<T>)
  // gets before delegating to ServiceLoader::load(Class<T>, ClassLoader)
  private var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

  private var decodeMode: DecodeMode = DecodeMode.Lenient
  private var cascadeMode: CascadeMode = CascadeMode.Merge
  private var allowEmptyConfigFiles = false
  private var allowNullOverride = false
  private var allowUnresolvedSubstitutions = false
  private var resolveTypesCaseInsensitive = false
  private var sealedTypeDiscriminatorField: String? = null
  private var contextResolverMode = ContextResolverMode.ErrorOnUnresolved

  private val propertySources = mutableListOf<PropertySource>()
  private val preprocessors = mutableListOf<Preprocessor>()
  private val nodeTransformers = mutableListOf<NodeTransformer>()
  private val resolvers = mutableListOf<Resolver>()
  private val paramMappers = mutableListOf<ParameterMapper>()
  private val parsers = mutableMapOf<String, Parser>()
  private val decoders = mutableListOf<Decoder<*>>()

  private var useReport: Boolean = false
  private var reportPrintFn: Print = { println(it) }
  private var secretsPolicy: SecretsPolicy = AllStringNodesSecretsPolicy
  private var obfuscator: Obfuscator = PrefixObfuscator(3)
  private var preprocessingIterations: Int = 1

  private var environment: Environment? = null
  private var flattenArraysToString: Boolean = false

  companion object {

    /**
     * Returns a [ConfigLoaderBuilder] with all defaults applied.
     *
     * This means that the default [Decoder]s, [Preprocessor]s, [NodeTransformer]s, [ParameterMapper]s,
     * [PropertySource]s, and [Parser]s are all registered.
     *
     * If you wish to avoid adding defaults, for example to avoid certain decoders or sources, then
     * use [empty] to obtain an empty ConfigLoaderBuilder and call the various addDefault methods manually.
     */
    fun default(): ConfigLoaderBuilder {
      return defaultWithoutPropertySources()
        .addDefaultPropertySources()
    }

    /**
     * Returns a [ConfigLoaderBuilder] with all defaults applied, except for [PropertySource]s.
     *
     * This means that the default [Decoder]s, [Preprocessor]s, [NodeTransformer]s, [ParameterMapper]s,
     * and [Parser]s are all registered.
     *
     * If you wish to avoid adding defaults, for example to avoid certain decoders or sources, then
     * use [empty] to obtain an empty ConfigLoaderBuilder and call the various addDefault methods manually.
     */
    fun defaultWithoutPropertySources(configure: ConfigLoaderBuilder.() -> Unit = { }): ConfigLoaderBuilder {
      return empty()
        .addDefaultDecoders()
        .addDefaultPreprocessors()
        .addDefaultNodeTransformers()
        .addDefaultParamMappers()
        .addDefaultParsers()
        .apply(configure)
    }

    /**
     * Returns a [ConfigLoaderBuilder] with all defaults applied, using resolvers in place of preprocessors.
     *
     * This means that the default [Decoder]s, [Resolver]s, [NodeTransformer]s, [ParameterMapper]s, [PropertySource]s,
     * and [Parser]s are all registered.
     *
     * If you wish to avoid adding defaults, for example to avoid certain decoders or sources, then
     * use [empty] to obtain an empty ConfigLoaderBuilder and call the various addDefault methods manually.
     *
     * Note: This new builder is experimental and may require breaking changes to your config files.
     * This builder will become the default in 3.0
     */
    @ExperimentalHoplite
    fun newBuilder(): ConfigLoaderBuilder {
      return newBuilderWithoutPropertySources().addDefaultPropertySources()
    }

    /**
     * Returns a [ConfigLoaderBuilder] with all defaults applied, using resolvers in place of preprocessors,
     * but without any [PropertySource]s.
     *
     * This means that the default [Decoder]s, [Resolver]s, [NodeTransformer]s, [ParameterMapper]s,
     * and [Parser]s are all registered.
     *
     * If you wish to avoid adding defaults, for example to avoid certain decoders or sources, then
     * use [empty] to obtain an empty ConfigLoaderBuilder and call the various addDefault methods manually.
     *
     * Note: This new builder is experimental and may require breaking changes to your config files.
     * This builder will become the default in 3.0
     */
    @ExperimentalHoplite
    fun newBuilderWithoutPropertySources(): ConfigLoaderBuilder {
      return empty()
        .addDefaultDecoders()
        .addDefaultResolvers()
        .addDefaultNodeTransformers()
        .addDefaultParamMappers()
        .addDefaultParsers()
    }

    /**
     * Returns a [ConfigLoaderBuilder] with no defaults applied.
     */
    fun empty(): ConfigLoaderBuilder = ConfigLoaderBuilder()

    /**
     * Returns a [ConfigLoaderBuilder] with no defaults applied and the given [configure]
     * function applied to the builder.
     */
    fun empty(configure: ConfigLoaderBuilder.() -> Unit): ConfigLoaderBuilder {
      val builder = ConfigLoaderBuilder()
      builder.configure()
      return builder
    }
  }

  fun withClassLoader(classLoader: ClassLoader): ConfigLoaderBuilder = apply {
    this.classLoader = classLoader
  }

  /**
   * Sets the current environment, eg prod or dev.
   */
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

  /**
   * Adds the default [Resolver]s to the end of the resolvers list.
   * Adding a resolver removes all preprocessors as the two do not work together.
   */
  fun addDefaultResolvers() = addResolvers(defaultResolvers())

  /**
   * Adds the given [Resolver] to the end of the resolvers list.
   * Adding a resolver removes all preprocessors as the two do not work together.
   */
  fun addResolver(resolver: Resolver) = addResolvers(setOf(resolver))

  /**
   * Adds the given [Resolver]s to the end of the resolvers list.
   */
  fun addResolvers(resolvers: Iterable<Resolver>): ConfigLoaderBuilder = apply {
    require(preprocessors.isEmpty()) {
      "Preprocessors cannot be used alongside resolvers. " +
        "Call removePreprocessors() before adding any resolver or create a ConfigLoaderBuilder using newBuilder() instead of default(). " +
        "Preprocessors will be removed in Hoplite 3.0"
    }
    this.resolvers.addAll(resolvers)
  }

  fun removePreprocessors() = apply {
    preprocessors.clear()
  }

  /**
   * Adds the given [Resolver]s to the end of the resolvers list.
   * Adding a resolver removes all preprocessors as the two do not work together.
   */
  fun addResolvers(vararg resolvers: Resolver): ConfigLoaderBuilder = addResolvers(resolvers.toList())

  fun addPreprocessor(preprocessor: Preprocessor) = addPreprocessors(listOf(preprocessor))

  fun addPreprocessors(preprocessors: Iterable<Preprocessor>): ConfigLoaderBuilder = apply {
    this.preprocessors.addAll(preprocessors)
  }

  fun addDefaultPreprocessors() = addPreprocessors(defaultPreprocessors())

  fun addNodeTransformer(nodeTransformer: NodeTransformer): ConfigLoaderBuilder = apply {
    this.nodeTransformers.add(nodeTransformer)
  }

  fun addNodeTransformers(nodeTransformers: Iterable<NodeTransformer>): ConfigLoaderBuilder = apply {
    this.nodeTransformers.addAll(nodeTransformers)
  }

  fun addDefaultNodeTransformers() = addNodeTransformers(defaultNodeTransformers())

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

  @Deprecated("Replaced with resolvers")
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
      .addDefaultNodeTransformers()
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
  When enabled, allows any [ConfigFilePropertySource] to be empty.
   */
  @Deprecated("use allowEmptyConfigFiles", ReplaceWith("allowEmptyConfigFiles()"))
  fun allowEmptyTree(): ConfigLoaderBuilder = allowEmptyConfigFiles()

  /**
  When enabled, allows any [ConfigFilePropertySource] to be empty.
   */
  @Deprecated("use allowEmptyConfigFiles", ReplaceWith("allowEmptyConfigFiles()"))
  fun allowEmptySources(): ConfigLoaderBuilder = allowEmptyConfigFiles()

  /**
   * When enabled, allows any [ConfigFilePropertySource] to be empty.
   */
  fun allowEmptyConfigFiles(): ConfigLoaderBuilder = apply {
    allowEmptyConfigFiles = true
  }

  /**
   * When enabled, allows config values to override to null.
   */
  fun allowNullOverride(): ConfigLoaderBuilder = apply {
    allowNullOverride = true
  }

  /**
   * When enabled, allows placeholder substitutions like ${foo} not to cause an error if they are not resolvable.
   */
  @Deprecated("Use ContextResolverMode", ReplaceWith("withContextResolverMode(ContextResolverMode.SkipUnresolved)"))
  fun allowUnresolvedSubstitutions(): ConfigLoaderBuilder = apply {
    allowUnresolvedSubstitutions = true
  }

  /**
   * When enabled, makes type/enum name resolution case-insensitive
   */
  fun withResolveTypesCaseInsensitive(): ConfigLoaderBuilder = apply {
    resolveTypesCaseInsensitive = true
  }

  fun withContextResolverMode(mode: ContextResolverMode) = apply {
    contextResolverMode = mode
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

  fun withReportPrintFn(reportPrintFn: (String) -> Unit): ConfigLoaderBuilder =
    apply { this.reportPrintFn = reportPrintFn }

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
    level = DeprecationLevel.ERROR
  )
  fun withReport(reporter: Reporter) = apply { useReport = true }

  @Deprecated(
    "use withReport(). Passing in a reporter no longer has any effect. Specify secrets policy and obfuscator directly on this builder.",
    ReplaceWith("withReport()"),
    level = DeprecationLevel.ERROR
  )
  fun report(reporter: Reporter) = apply { useReport = true }

  /**
   * Set a field name to be used as the discriminator field for sealed types.
   *
   * Then, Hoplite will use this field to pick amongst the sealed types instead of trying to
   * infer the type from the available config values.
   *
   * This option will become the default in 3.0.
   */
  @ExperimentalHoplite
  fun withExplicitSealedTypes(discriminatorFieldName: String = "_type"): ConfigLoaderBuilder =
    apply { sealedTypeDiscriminatorField = discriminatorFieldName }

  fun build(): ConfigLoader {
    return ConfigLoader(
      decoderRegistry = DefaultDecoderRegistry(decoders),
      propertySources = propertySources.toList(),
      parserRegistry = DefaultParserRegistry(parsers),
      preprocessors = preprocessors.toList(),
      nodeTransformers = nodeTransformers.toList(),
      paramMappers = paramMappers.toList(),
      onFailure = failureCallbacks.toList(),
      resolvers = resolvers,
      decodeMode = decodeMode,
      useReport = useReport,
      allowEmptyTree = allowEmptyConfigFiles,
      allowNullOverride = allowNullOverride,
      resolveTypesCaseInsensitive = resolveTypesCaseInsensitive,
      allowUnresolvedSubstitutions = allowUnresolvedSubstitutions,
      preprocessingIterations = preprocessingIterations,
      cascadeMode = cascadeMode,
      secretsPolicy = secretsPolicy,
      environment = environment,
      obfuscator = obfuscator,
      reportPrintFn = reportPrintFn,
      flattenArraysToString = flattenArraysToString,
      sealedTypeDiscriminatorField = sealedTypeDiscriminatorField,
      contextResolverMode = contextResolverMode,
    )
  }
}

fun defaultPropertySources(): List<PropertySource> = listOfNotNull(
  EnvironmentVariablesPropertySource(),
  SystemPropertiesPropertySource,
  UserSettingsPropertySource,
  XdgConfigPropertySource,
)

fun emptyByDefaultPropertySources(): List<PropertySource> = listOfNotNull(
  SystemPropertiesPropertySource,
  UserSettingsPropertySource,
  XdgConfigPropertySource,
)

fun defaultPreprocessors(): List<Preprocessor> = listOf(
  EnvOrSystemPropertyPreprocessor,
  RandomPreprocessor,
  LookupPreprocessor,
)

fun defaultNodeTransformers(): List<NodeTransformer> = listOf(
  PathNormalizer,
)

fun defaultResolvers(): List<Resolver> = listOf(
  EnvVarContextResolver,
  SystemPropertyContextResolver,
  ReferenceContextResolver,
  HopliteContextResolver,
  SystemContextResolver,
  ManifestContextResolver,
  RandomContextResolver,
)

fun defaultParamMappers(): List<ParameterMapper> = listOf(
  DefaultParamMapper,
  LowercaseParamMapper,
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
