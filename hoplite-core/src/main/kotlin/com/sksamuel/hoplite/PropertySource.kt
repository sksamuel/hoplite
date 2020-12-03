package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMapInvalid
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.parsers.Parser
import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.parsers.defaultParserRegistry
import com.sksamuel.hoplite.parsers.toNode
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * A [PropertySource] provides [Node]s.
 * A source may retrieve its values from a config file, or env variables, and so on.
 */
interface PropertySource {
  fun node(): ConfigResult<Node>

  companion object {

    /**
     * Returns a [PropertySource] that will read the specified resource from the classpath.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun resource(resource: String, optional: Boolean = false) =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional)

    /**
     * Returns a [PropertySource] that will read the specified file from the filesystem.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun file(file: File, optional: Boolean = false) =
      ConfigFilePropertySource(ConfigSource.FileSource(file), optional = optional)

    /**
     * Returns a [PropertySource] that will read the specified resource from the classpath.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun path(path: Path, optional: Boolean = false) =
      ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional)
  }
}

fun defaultPropertySources(registry: ParserRegistry): List<PropertySource> =
  listOf(
    EnvironmentVariablesPropertySource(true, false),
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

class EnvironmentVariablesPropertySource(
  private val useUnderscoresAsSeparator: Boolean,
  private val allowUppercaseNames: Boolean
) : PropertySource {
  override fun node(): ConfigResult<Node> {
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
 * An implementation of [PropertySource] that provides config via an [InputStream].
 * You must specify the config type in addition to the stream source.
 */
class InputStreamPropertySource(
  private val input: InputStream,
  private val ext: String,
  private val parserRegistry: ParserRegistry = defaultParserRegistry()
) : PropertySource {

  override fun node(): ConfigResult<Node> {
    return parserRegistry.locate(ext).map {
      it.load(input, "input-stream")
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
  private val parserRegistry: ParserRegistry = defaultParserRegistry(),
  private val optional: Boolean = false
) : PropertySource {
  override fun node(): ConfigResult<Node> {
    val parser = parserRegistry.locate(config.ext())
    val input = config.open()
    return Validated.ap(parser, input) { a, b -> a.load(b, config.describe()) }
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatMapInvalid { if (optional) Undefined.valid() else it.invalid() }
  }

  companion object {

    fun optionalPath(
      path: Path,
      registry: ParserRegistry = defaultParserRegistry()
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.PathSource(path), registry, true)

    fun optionalFile(
      file: File,
      registry: ParserRegistry = defaultParserRegistry()
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.FileSource(file), registry, true)

    fun optionalResource(
      resource: String,
      registry: ParserRegistry = defaultParserRegistry()
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), registry, true)
  }
}

