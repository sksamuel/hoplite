package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMapInvalid
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import com.sksamuel.hoplite.parsers.toNode
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * A [PropertySource] provides [Node]s.
 * A source may retrieve its values from a config file, or env variables, and so on.
 */
interface PropertySource {
  fun node(): ConfigResult<Node>
}

fun defaultPropertySources(registry: ParserRegistry): List<PropertySource> =
  listOf(
    EnvironmentVariablesPropertySource(true),
    SystemPropertiesPropertySource,
    UserSettingsPropertySource(registry)
  )

/**
 * An implementation of [PropertySource] that provides config through system properties
 * that are prefixed with 'config.override.'
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 */
object SystemPropertiesPropertySource : PropertySource {
  private const val prefix = "config.override."
  override fun node(): ConfigResult<Node> {
    val props = Properties()
    System.getProperties()
      .stringPropertyNames()
      .filter { it.startsWith(prefix) }
      .forEach { props[it.removePrefix(prefix)] = System.getProperty(it) }
    return if (props.isEmpty) Undefined.valid() else props.toNode("sysprops").valid()
  }
}

class EnvironmentVariablesPropertySource(private val useUnderscoresAsSeparator: Boolean) : PropertySource {
  override fun node(): ConfigResult<Node> {
    val props = Properties()
    System.getenv().forEach {
      val key = if (useUnderscoresAsSeparator) it.key.replace("__", ".") else it.key
      props[key] = it.value
    }
    return props.toNode("env").valid()
  }
}

/**
 * An implementation of [PropertySource] that provides config through a config file
 * defined at ~/.userconfig.ext
 *
 * This file must use either the java properties format, or another format that you
 * have included the correct module for.
 *
 * Eg, if you have included hoplite-yaml module in your build, then your file can be
 * ~/.userconfig.yaml
 */
class UserSettingsPropertySource(private val parserRegistry: ParserRegistry) : PropertySource {

  private fun path(ext: String): Path = Paths.get(System.getProperty("user.home")).resolve(".userconfig.$ext")

  override fun node(): ConfigResult<Node> {
    val ext = parserRegistry.registeredExtensions().firstOrNull {
      path(it).toFile().exists()
    }
    return if (ext == null) Undefined.valid() else {
      val path = path(ext)
      val input = path.toFile().inputStream()
      parserRegistry.locate(ext).map {
        it.load(input, path.toString())
      }
    }
  }
}

/**
 * An implementation of [PropertySource] that loads values from a file located
 * via a [ConfigSource]. The file is parsed using an instance of [Parser] retrieved
 * from the [ParserRegistry] based on file extension.
 *
 * @param optional if true then if a file is missing, this property source will be skipped. If false, then a missing
 * file will cause the config to fail. Defaults to false.
 */
class ConfigFilePropertySource(private val config: ConfigSource,
                               private val parserRegistry: ParserRegistry = defaultParserRegistry(),
                               private val optional: Boolean = false) : PropertySource {
  override fun node(): ConfigResult<Node> {
    val parser = parserRegistry.locate(config.ext())
    val input = config.open()
    return Validated.ap(parser, input) { a, b -> a.load(b, config.describe()) }
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatMapInvalid { if (optional) Undefined.valid() else it.invalid() }
  }

  companion object {
    fun optionalResource(resource: String,
                         registry: ParserRegistry = defaultParserRegistry()): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), registry, true)
  }
}

