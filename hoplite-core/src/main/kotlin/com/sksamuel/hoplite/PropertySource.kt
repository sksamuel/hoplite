package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.ParserRegistry
import com.sksamuel.hoplite.sources.CommandLinePropertySource
import com.sksamuel.hoplite.sources.ConfigFilePropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.InputStreamPropertySource
import com.sksamuel.hoplite.sources.MapPropertySource
import java.io.File
import java.io.InputStream
import java.nio.file.Path

data class PropertySourceContext(
  val parsers: ParserRegistry,
  val allowEmptyConfigFiles: Boolean = false,
) {
  companion object {
    val empty = PropertySourceContext(ParserRegistry.empty, false)
  }
}

/**
 * A [PropertySource] provides a tree of config values.
 *
 * This tree of config values is rooted with a [Node].
 *
 * A [PropertySource] may retrieve its values from a number of sources, such as config files,
 *  environment variables, system properties, AWS secrets manager, and so on.
 */
interface PropertySource {

  /**
   * Returns the root [Node] provided by this property source, or an error
   * if the node could not be constructed.
   */
  fun node(context: PropertySourceContext): ConfigResult<Node>

  /**
   * Return a string detailing the location of this source, eg file://myfile
   */
  fun source(): String

  companion object {

    /**
     * Returns a [PropertySource] that will read the specified resource from the classpath.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun resource(resource: String, optional: Boolean = false, allowEmpty: Boolean = false) =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional, allowEmpty)

    /**
     * Returns a [PropertySource] that will read the specified file from the filesystem.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun file(file: File, optional: Boolean = false, allowEmpty: Boolean = false) =
      path(file.toPath(), optional, allowEmpty)

    /**
     * Returns a [PropertySource] that will read the specified resource from the classpath.
     *
     * @param optional if true then the resource can not exist and the config loader will ignore this source
     */
    fun path(path: Path, optional: Boolean = false, allowEmpty: Boolean = false) =
      ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional, allowEmpty = allowEmpty)

    /**
     * Returns a [PropertySource] that will read the specified input stream.
     *
     * @param input the input stream to read from
     * @param ext the file extension of the input format
     */
    fun stream(input: InputStream, ext: String) =
      InputStreamPropertySource(input, ext, "$ext input stream")

    /**
     * Returns a [PropertySource] that read the specified map values.
     *
     * @param map map
     */
    fun map(map: Map<String, Any>) = MapPropertySource(map)

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
     * Returns a [PropertySource] that will read the environment settings, by default with the classic
     * parsing mechanism using double-underscore as a path separator, and converting uppercase names with
     * underscores to camel case.
     */
    fun environment() =
      EnvironmentVariablesPropertySource()

    /**
     * Returns a [PropertySource] that will read from the specified string.
     *
     * @param str the string to read from
     * @param ext the file extension of the input format
     */
    fun string(str: String, ext: String) =
      InputStreamPropertySource(str.byteInputStream(), ext, "$ext string source")

  }
}
