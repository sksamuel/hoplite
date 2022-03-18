package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.ParserRegistry
import java.io.File
import java.io.InputStream
import java.nio.file.Path

data class PropertySourceContext(
  val parsers: ParserRegistry,
)

/**
 * A [PropertySource] provides a tree of config values.
 *
 * This tree of config values is rooted with a [Node].
 *
 * A property source may retrieve its values from a config file, or env variables, system properties, and so on,
 * depending on the implementation.
 */
interface PropertySource {

  /**
   * Returns the root [Node] provided by this property source, or an error
   * if the values could not be retrieved.
   */
  fun node(context: PropertySourceContext): ConfigResult<Node>

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

    /**
     * Returns a [PropertySource] that will read the specified input stream.
     *
     * @param input the input stream to read from
     * @param ext the file extension of the input format
     */
    fun stream(input: InputStream, ext: String) =
      InputStreamPropertySource(input, ext)

    /**
     * Returns a [PropertySource] that read the specified map values.
     *
     * @param map map
     */
    fun map(map: Map<String, Any>) =
      MapPropertySource(map)

    /**
     * Returns a [PropertySource] that will read the specified command line arguments.
     *
     * @param arguments command line arguments as passed to main method
     * @param prefix argument prefix
     * @param delimiter key value delimiter
     */
    fun commandLine(arguments: Array<String>, prefix: String = "--", delimiter: String = "=") =
      CommandLinePropertySource(arguments, prefix, delimiter)

    /**
     * Returns a [PropertySource] that will read the environment settings.
     */
    fun environment(useUnderscoresAsSeparator: Boolean = true, allowUppercaseNames: Boolean = true) =
      EnvironmentVariablesPropertySource(useUnderscoresAsSeparator, allowUppercaseNames)


    /**
     * Returns a [PropertySource] that will read from the specified string.
     *
     * @param str the string to read from
     * @param ext the file extension of the input format
     */
    fun string(str: String, ext: String) =
      stream(str.byteInputStream(), ext)

  }
}

fun defaultPropertySources(): List<PropertySource> = listOf(
  SystemPropertiesPropertySource,
  UserSettingsPropertySource
)
