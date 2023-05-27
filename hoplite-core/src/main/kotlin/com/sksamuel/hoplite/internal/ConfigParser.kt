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
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Print
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.resolver.Resolver
import com.sksamuel.hoplite.resolver.Resolving
import com.sksamuel.hoplite.secrets.Obfuscator
import com.sksamuel.hoplite.secrets.SecretsPolicy
import kotlin.reflect.KClass

class ConfigParser(
  classpathResourceLoader: ClasspathResourceLoader,
  parserRegistry: ParserRegistry,
  allowEmptyTree: Boolean,
  cascadeMode: CascadeMode,
  preprocessors: List<Preprocessor>,
  preprocessingIterations: Int,
  private val resolvers: List<Resolver>,
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val flattenArraysToString: Boolean,
  private val allowUnresolvedSubstitutions: Boolean,
  private val secretsPolicy: SecretsPolicy?,
  private val decodeMode: DecodeMode,
  private val useReport: Boolean,
  private val obfuscator: Obfuscator,
  private val reportPrintFn: Print,
  private val environment: Environment?,
  private val sealedTypeDiscriminatorField: String?,
) {

  private val loader = PropertySourceLoader(classpathResourceLoader, parserRegistry, allowEmptyTree)
  private val cascader = Cascader(cascadeMode, allowEmptyTree)
  private val preprocessing = Preprocessing(preprocessors, preprocessingIterations)
  private val decoding = Decoding(decoderRegistry, secretsPolicy)

  private fun context(root: Node): DecoderContext {
    return DecoderContext(
      decoders = decoderRegistry,
      paramMappers = paramMappers,
      config = DecoderConfig(flattenArraysToString),
      environment = environment,
      resolvers = Resolving(resolvers, root),
      sealedTypeDiscriminatorField = sealedTypeDiscriminatorField,
    )
  }

  fun <A : Any> decode(
    kclass: KClass<A>,
    environment: Environment?,
    resourceOrFiles: List<String>,
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>,
  ): ConfigResult<A> {

    if (decoderRegistry.size == 0)
      return ConfigFailure.EmptyDecoderRegistry.invalid()

    return loader.loadNodes(propertySources, configSources, resourceOrFiles).flatMap { nodes ->
      cascader.cascade(nodes).flatMap { node ->
        val context = context(node)
        preprocessing.preprocess(node, context).flatMap { preprocessed ->
          check(preprocessed).flatMap {

            val decoded = decoding.decode(kclass, preprocessed, decodeMode, context)
            val state = createDecodingState(preprocessed, context, secretsPolicy)

            // always do report regardless of decoder result
            if (useReport) {
              Reporter(reportPrintFn, obfuscator, environment)
                .printReport(propertySources, state, context.reports)
            }

            decoded
          }
        }
      }
    }
  }

  fun load(
    resourceOrFiles: List<String>,
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>,
  ): ConfigResult<Node> {
    return loader.loadNodes(propertySources, configSources, resourceOrFiles).flatMap { nodes ->
      cascader.cascade(nodes).flatMap { node ->
        val context = context(node)
        preprocessing.preprocess(node, context).flatMap { preprocessed ->
          if (allowUnresolvedSubstitutions) preprocessed.valid() else UnresolvedSubstitutionChecker.process(preprocessed)
        }
      }
    }
  }

  private fun check(node: Node): ConfigResult<Node> {
    // if resolvers is not empty, we don't use allowUnresolvedSubstitutions, but instead the resolver mode setting
    return if (allowUnresolvedSubstitutions || resolvers.isNotEmpty())
      node.valid()
    else
      UnresolvedSubstitutionChecker.process(node)
  }
}
