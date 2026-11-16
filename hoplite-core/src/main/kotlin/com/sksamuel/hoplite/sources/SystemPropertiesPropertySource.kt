package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.CommonMetadata
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.toNode
import com.sksamuel.hoplite.transform
import com.sksamuel.hoplite.withMeta
import java.util.Properties

/**
 * An implementation of [PropertySource] that provides config through system properties
 * that are prefixed with [prefix] (defaults to 'config.override.').
 *
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 *
 * The [prefix] may be overridden to use a different value, or set to an empty string
 * to expose all system properties.
 */
open class SystemPropertiesPropertySource(
  private val systemPropertiesMap: () -> Map<String, String> = { System.getProperties().toStringMap() },
  private val prefix: String = DEFAULT_PREFIX,
) : PropertySource {

  override fun source(): String = "System Properties"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val map = systemPropertiesMap().filter { it.key.startsWith(prefix) }
    if (map.isEmpty()) return Undefined.valid()
    val node = map.toNode("sysprops") { it.removePrefix(prefix) }
    // With an empty prefix, the source sees every JVM system property, so it is ambient (#606).
    return (if (prefix.isEmpty()) node.transform { it.withMeta(CommonMetadata.Ambient, true) } else node).valid()
  }

  companion object : SystemPropertiesPropertySource() {
    const val DEFAULT_PREFIX = "config.override."

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
