package com.sksamuel.hoplite

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
    val props = Properties()
    systemPropertiesMap().let { systemPropertiesMap ->
      systemPropertiesMap.keys
        .filter { it.startsWith(prefix) }
        .forEach { props[it.removePrefix(prefix)] = systemPropertiesMap[it] }
    }
    return if (props.isEmpty) Undefined.valid() else props.toNode("sysprops").valid()
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
