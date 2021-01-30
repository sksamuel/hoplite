package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMapInvalid
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.toNode
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

fun defaultPropertySources(): List<PropertySource> =
  listOf(
    EnvironmentVariablesPropertySource(useUnderscoresAsSeparator = true, allowUppercaseNames = false),
    SystemPropertiesPropertySource,
    UserSettingsPropertySource
  )

/**
 * An implementation of [PropertySource] that provides config through system properties
 * that are prefixed with 'config.override.'
 * In other words, if a System property is defined 'config.override.user.name=sam' then
 * the property 'user.name=sam' is made available.
 */
object SystemPropertiesPropertySource : PropertySource {
  private const val prefix = "config.override."
  override fun node(parsers: ParserRegistry): ConfigResult<Node> {
    val props = Properties()
    System.getProperties()
      .stringPropertyNames()
      .filter { it.startsWith(prefix) }
      .forEach { props[it.removePrefix(prefix)] = System.getProperty(it) }
    return if (props.isEmpty) Undefined.valid() else props.toNode("sysprops").valid()
  }
}

class EnvironmentVariablesPropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val allowUppercaseNames: Boolean
) : PropertySource {
  override fun node(parsers: ParserRegistry): ConfigResult<Node> {
    val props = Properties()
    System.getenv().forEach {
      val key = it.key
        .let { key -> if (useUnderscoresAsSeparator) key.replace("__", ".") else key }
        .let { key ->
          if (allowUppercaseNames && Character.isUpperCase(key.codePointAt(0))) {
            key.split(".").joinToString(separator = ".") { value ->
              value.fold("") { acc, char ->
                when {
                  acc.isEmpty() -> acc + char.toLowerCase()
                  acc.last() == '_' -> acc.dropLast(1) + char.toUpperCase()
                  else -> acc + char.toLowerCase()
                }
              }
            }
          } else {
            key
          }
        }
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
object UserSettingsPropertySource : PropertySource {

  private fun path(ext: String): Path = Paths.get(System.getProperty("user.home")).resolve(".userconfig.$ext")

  override fun node(parsers: ParserRegistry): ConfigResult<Node> {
    val ext = parsers.registeredExtensions().firstOrNull {
      path(it).toFile().exists()
    }
    return if (ext == null) Undefined.valid() else {
      val path = path(ext)
      val input = path.toFile().inputStream()
      parsers.locate(ext).map {
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
class ConfigFilePropertySource(
  private val config: ConfigSource,
  private val optional: Boolean = false
) : PropertySource {

  override fun node(parsers: ParserRegistry): ConfigResult<Node> {
    val parser = parsers.locate(config.ext())
    val input = config.open()
    return Validated.ap(parser, input) { a, b -> a.load(b, config.describe()) }
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatMapInvalid { if (optional) Undefined.valid() else it.invalid() }
  }

  companion object {

    fun optionalPath(
      path: Path,
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.PathSource(path), true)

    fun optionalFile(
      file: File,
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.FileSource(file), true)

    fun optionalResource(
      resource: String,
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), true)
  }
}

