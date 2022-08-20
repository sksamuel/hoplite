package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

/**
 * An implementation of [PropertySource] that simply wraps a [Map].
 */
class MapPropertySource(
  private val map: Map<String, Any?>,
) : PropertySource {

  override fun source(): String = "Provided Map"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return when {
      map.isEmpty() -> Undefined.valid()
      else -> map.toNode("map").valid()
    }
  }
}
