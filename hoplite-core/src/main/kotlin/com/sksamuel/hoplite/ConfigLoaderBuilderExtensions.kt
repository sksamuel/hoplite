package com.sksamuel.hoplite

import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param resource the resource to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addResourceSource(resource: String, optional: Boolean = false) = addPropertySource(
  ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified file from the filesystem.
 *
 * @param file the [File] to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addFileSource(file: File, optional: Boolean = false) = addPropertySource(
  ConfigFilePropertySource(ConfigSource.FileSource(file), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param path the [Path] of the resource to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addPathSource(path: Path, optional: Boolean = false) = addPropertySource(
  ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified input stream.
 *
 * @param input the [InputStream] to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoaderBuilder.addStreamSource(input: InputStream, ext: String) = addPropertySource(
  InputStreamPropertySource(input, ext)
)

/**
 * Returns a [PropertySource] that will read the specified map values.
 *
 * @param map the [Map] to read from
 */
fun ConfigLoaderBuilder.addMapSource(map: Map<String, Any>) = addPropertySource(
  MapPropertySource(map)
)

/**
 * Returns a [PropertySource] that will read the specified command line arguments.
 *
 * @param arguments command line arguments as passed to main method
 * @param prefix argument prefix
 * @param delimiter key value delimiter
 */
fun ConfigLoaderBuilder.addCommandLineSource(
  arguments: Array<String>,
  prefix: String = "--",
  delimiter: String = "=",
) = addPropertySource(
  CommandLinePropertySource(arguments, prefix, delimiter)
)

/**
 * Returns a [PropertySource] that will read the environment settings.
 *
 * @param useUnderscoresAsSeparator if true, use double underscore instead of period to separate keys in nested config
 * @param allowUppercaseNames if true, allow uppercase-only names
 */
fun ConfigLoaderBuilder.addEnvironmentSource(
  useUnderscoresAsSeparator: Boolean = true,
  allowUppercaseNames: Boolean = true,
) = addPropertySource(
  EnvironmentVariablesPropertySource(useUnderscoresAsSeparator, allowUppercaseNames)
)

/**
 * Returns a [PropertySource] that will read from the specified string.
 *
 * @param str the [String] to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoaderBuilder.string(
  str: String, ext: String
) = addStreamSource(str.byteInputStream(), ext)
