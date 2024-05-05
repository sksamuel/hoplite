package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import java.util.Properties

/**
 * An implementation of [PropertySource] that provides config through system properties
 * that are prefixed with 'config.override.'
 *
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 */
open class SystemPropertiesPropertySource(
  private val systemPropertiesMap: () -> Map<String, String> = { System.getProperties().toStringMap() }
) : PropertySource {

  override fun source(): String = "System Properties"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = systemPropertiesMap().filter { it.key.startsWith(prefix) }
    return if (map.isEmpty()) Undefined.valid() else map.toNode("sysprops") {
      it.removePrefix(prefix)
    }.valid()
  }

  companion object : SystemPropertiesPropertySource() {
    private const val prefix = "config.override."

    // for backwards compatibility with Java from when SystemPropertiesPropertySource was an `object`
    @Suppress("unused")
    @JvmField
    val INSTANCE: SystemPropertiesPropertySource = this
  }
}

internal fun Properties.toStringMap(): Map<String, String> = this.let { systemProperties ->
  systemProperties.stringPropertyNames().associateWith { propertyName ->
    systemProperties.getProperty(propertyName)
  }
}
