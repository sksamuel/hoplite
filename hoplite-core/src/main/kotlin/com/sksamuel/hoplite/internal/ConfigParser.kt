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
import com.sksamuel.hoplite.env.ServiceName
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.report.Reporter
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
  private val decoderRegistry: DecoderRegistry,
  private val paramMappers: List<ParameterMapper>,
  private val flattenArraysToString: Boolean,
  private val allowUnresolvedSubstitutions: Boolean,
  private val secretsPolicy: SecretsPolicy?,
  private val decodeMode: DecodeMode,
  private val useReport: Boolean,
  private val obfuscator: Obfuscator,
  private val environment: Environment?,
  private val serviceName: ServiceName?,
) {

  private val loader = PropertySourceLoader(classpathResourceLoader, parserRegistry, allowEmptyTree)
  private val cascader = Cascader(cascadeMode, allowEmptyTree)
  private val preprocessing = Preprocessing(preprocessors, preprocessingIterations)
  private val decoding = Decoding(decoderRegistry, secretsPolicy)

  fun <A : Any> decode(
    kclass: KClass<A>,
    environment: Environment?,
    resourceOrFiles: List<String>,
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>,
  ): ConfigResult<A> {

    if (decoderRegistry.size == 0)
      return ConfigFailure.EmptyDecoderRegistry.invalid()

    val context = DecoderContext(
      decoders = decoderRegistry,
      paramMappers = paramMappers,
      config = DecoderConfig(flattenArraysToString),
      environment = environment,
      serviceName = serviceName,
    )

    return loader.loadNodes(propertySources, configSources, resourceOrFiles)
      .flatMap { cascader.cascade(it) }
      .flatMap { preprocessing.preprocess(it, context) }
      .flatMap { if (allowUnresolvedSubstitutions) it.valid() else UnresolvedSubstitutionChecker.process(it) }
      .flatMap { preprocessed ->

        val decoded = decoding.decode(kclass, preprocessed, decodeMode, context)
        val state = createDecodingState(preprocessed, context, secretsPolicy)

        // always do report regardless of decoder result
        if (useReport) {
          Reporter({ println(it) }, obfuscator, environment, serviceName)
            .printReport(propertySources, state, context.reports)
        }

        decoded
      }

  }

  fun load(
    resourceOrFiles: List<String>,
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>,
  ): ConfigResult<Node> {

    val context = DecoderContext(
      decoders = decoderRegistry,
      paramMappers = paramMappers,
      config = DecoderConfig(flattenArraysToString),
      environment = environment,
      serviceName = serviceName,
    )

    return loader.loadNodes(propertySources, configSources, resourceOrFiles)
      .flatMap { cascader.cascade(it) }
      .flatMap { preprocessing.preprocess(it, context) }
      .flatMap { if (allowUnresolvedSubstitutions) it.valid() else UnresolvedSubstitutionChecker.process(it) }
  }
}
