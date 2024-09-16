package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ClasspathResourceLoader
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.DecoderConfig
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ParameterMapper
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.decoder.DecoderRegistry
import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.transformer.NodeTransformer
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.resolver.Resolving
import com.sksamuel.hoplite.resolver.context.ContextResolverMode
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import com.sksamuel.hoplite.transformer.PathNormalizer
import kotlin.reflect.KClass

class ConfigParser(
  classpathResourceLoader: ClasspathResourceLoader,
  parserRegistry: ParserRegistry,
  allowEmptyTree: Boolean,
  allowNullOverride: Boolean,
  cascadeMode: CascadeMode,
  preprocessors: List<Preprocessor>,
  preprocessingIterations: Int,
  private val nodeTransformers: List<NodeTransformer>,
  private val resolvers: List<Resolver>,
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val flattenArraysToString: Boolean,
  private val resolveTypesCaseInsensitive: Boolean,
  private val allowUnresolvedSubstitutions: Boolean,
  private val secretsPolicy: SecretsPolicy?,
  private val decodeMode: DecodeMode,
  private val useReport: Boolean,
  private val obfuscator: Obfuscator,
  private val reportPrintFn: Print,
  private val environment: Environment?,
  private val sealedTypeDiscriminatorField: String?,
  private val contextResolverMode: ContextResolverMode,
  private val resourceOrFiles: List<String>,
  private val propertySources: List<PropertySource>,
  private val configSources: List<ConfigSource>,
) {

  private val loader = PropertySourceLoader(nodeTransformers, sealedTypeDiscriminatorField, classpathResourceLoader, parserRegistry, allowEmptyTree)
  private val cascader = Cascader(cascadeMode, allowEmptyTree, allowNullOverride)
  private val preprocessing = Preprocessing(preprocessors, preprocessingIterations)
  private val decoding = Decoding(decoderRegistry, secretsPolicy)
  private lateinit var configResult: ConfigResult<Node>
  private lateinit var context: DecoderContext

  init {
    loadResultAndContext()
  }

  private fun context(root: Node): DecoderContext {
    return DecoderContext(
      decoders = decoderRegistry,
      paramMappers = paramMappers,
      config = DecoderConfig(flattenArraysToString, resolveTypesCaseInsensitive),
      environment = environment,
      resolvers = Resolving(resolvers, root),
      sealedTypeDiscriminatorField = sealedTypeDiscriminatorField,
      contextResolverMode = contextResolverMode,
    )
  }

  fun <A : Any> decode(
    kclass: KClass<A>,
    environment: Environment?,
    prefix: String? = null,
  ): ConfigResult<A> {

    if (configResult.isInvalid()) {
      return configResult.getInvalidUnsafe().invalid()
    }

    return configResult
      .map { it.prefixedNode(prefix) }
      .flatMap {
        val decoded = decoding.decode(kclass, it, decodeMode, context)
        val state = createDecodingState(it, context, secretsPolicy)

        // always do report regardless of decoder result
        if (useReport) {
          Reporter(reportPrintFn, obfuscator, environment, prefix)
            .printReport(propertySources, state, context.reporter.getReport())
        }

        decoded
      }
  }

  fun load(): ConfigResult<Node> = configResult

  private fun loadResultAndContext() {
    if (decoderRegistry.size == 0) {
      configResult = ConfigFailure.EmptyDecoderRegistry.invalid()
      return
    }

    loader.loadNodes(propertySources, configSources, resourceOrFiles).flatMap { nodes ->
      cascader.cascade(nodes).flatMap { node ->
        val context = context(node)
        preprocessing.preprocess(node, context).flatMap { preprocessed ->
          check(preprocessed)
        }.map {
          it to context
        }
      }
    }.onSuccess { result ->
      configResult = result.first.valid()
      context = result.second
    }.onFailure { result ->
      configResult = result.invalid()
    }
  }

  private fun check(node: Node): ConfigResult<Node> {
    // if resolvers is not empty, we don't use allowUnresolvedSubstitutions, but instead the resolver mode setting
    return if (resolvers.isNotEmpty() || allowUnresolvedSubstitutions || contextResolverMode == ContextResolverMode.SkipUnresolved)
      node.valid()
    else
      UnresolvedSubstitutionChecker.process(node)
  }

  private fun Node.prefixedNode(prefix: String?) = when {
    prefix == null -> this
    nodeTransformers.contains(PathNormalizer) -> atPath(PathNormalizer.normalizePathElement(prefix))
    else -> atPath(prefix)
  }
}
