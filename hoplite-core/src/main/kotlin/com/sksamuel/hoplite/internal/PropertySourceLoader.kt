package com.sksamuel.hoplite.internal

import com.sksamuel.hoplite.ClasspathResourceLoader
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.NonEmptyList
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.sources.ConfigFilePropertySource

/**
 * Loads [Node]s from [PropertySource]s, [ConfigSource]s, files and classpath resources.
 */
class PropertySourceLoader(
  private val classpathResourceLoader: ClasspathResourceLoader,
  private val parserRegistry: ParserRegistry,
  private val allowEmptyPropertySources: Boolean,
) {

  fun loadNodes(
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>,
    resourceOrFiles: List<String>,
  ): ConfigResult<NonEmptyList<Node>> {
    require(propertySources.isNotEmpty() || configSources.isNotEmpty() || resourceOrFiles.isNotEmpty())

    return ConfigSource
      .fromResourcesOrFiles(resourceOrFiles.toList(), classpathResourceLoader)
      .map { sources ->
        propertySources + (configSources + sources).map {
          ConfigFilePropertySource(
            it,
            optional = false,
            allowEmpty = allowEmptyPropertySources
          )
        }
      }
      .flatMap { if (it.isEmpty()) ConfigFailure.NoSources.invalid() else it.valid() }
      .flatMap { loadSources(it) }
  }

  private fun loadSources(sources: List<PropertySource>): ConfigResult<NonEmptyList<Node>> {
    return sources
      .map { it.node(PropertySourceContext(parserRegistry)) }
      .sequence()
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatMap { if (it.isEmpty()) ConfigFailure.NoSources.invalid() else NonEmptyList.unsafe(it).valid() }
  }
}
