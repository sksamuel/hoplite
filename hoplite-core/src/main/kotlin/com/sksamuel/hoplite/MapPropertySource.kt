package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode

/**
 * An implementation of [PropertySource] that provides config based on command line arguments.
 *
 * Parameters will be processed if they start with a given prefix. Key and value are split by a given delimiter.
 */
class MapPropertySource(
  private val map: Map<String, Any?>,
) : PropertySource {
  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return when {
      map.isEmpty() -> Undefined.valid()
      else -> map.toNode("map").valid()
    }
  }
}
