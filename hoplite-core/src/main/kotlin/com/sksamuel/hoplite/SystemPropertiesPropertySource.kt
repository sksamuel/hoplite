package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import java.util.Properties

/**
 * An implementation of [PropertySource] that provides config through system properties
 * that are prefixed with 'config.override.'
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 */
object SystemPropertiesPropertySource : PropertySource {
  private const val prefix = "config.override."
  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val props = Properties()
    System.getProperties()
      .stringPropertyNames()
      .filter { it.startsWith(prefix) }
      .forEach { props[it.removePrefix(prefix)] = System.getProperty(it) }
    return if (props.isEmpty) Undefined.valid() else props.toNode("sysprops").valid()
  }
}
