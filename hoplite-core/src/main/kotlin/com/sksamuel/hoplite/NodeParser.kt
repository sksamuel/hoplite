package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.sources.ConfigFilePropertySource

class NodeParser(private val parserRegistry: ParserRegistry) {

  /**
   * Loads all property sources and combines them into a single node.
   */
  fun parseNode(
    propertySources: List<PropertySource>,
    configSources: List<ConfigSource>
  ): ConfigResult<NodeResult> {
    require(propertySources.isNotEmpty() || configSources.isNotEmpty())

    val combinedPropertySources = propertySources + configSources.map { ConfigFilePropertySource(it) }
    if (combinedPropertySources.isEmpty()) return ConfigFailure.NoSources.invalid()

    return combinedPropertySources
      .map { it.node(PropertySourceContext(parserRegistry)) }
      .sequence()
      .mapInvalid {
        val multipleFailures = ConfigFailure.MultipleFailures(it)
        multipleFailures
      }.flatMap { nodes ->
        // nodes cannot be empty, as srcs is not empty, and if any of them errored, we would not be in this map block
        val reduced = nodes.reduce { a, b -> a.merge(b) }
        if (reduced == Undefined)
          ConfigFailure.NoValues.invalid()
        else
          NodeResult(combinedPropertySources, reduced).valid()
      }
  }
}

data class NodeResult(
  val propertySources: List<PropertySource>,
  val node: Node,
)
