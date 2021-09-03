package com.sksamuel.hoplite

import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun ConfigLoader.Builder.addResourceSource(resource: String, optional: Boolean = false) = addSource(
  ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified file from the filesystem.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun ConfigLoader.Builder.addFileSource(file: File, optional: Boolean = false) = addSource(
  ConfigFilePropertySource(ConfigSource.FileSource(file), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun ConfigLoader.Builder.addPathSource(path: Path, optional: Boolean = false) = addSource(
  ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional)
)

/**
 * Returns a [PropertySource] that will read the specified input stream.
 *
 * @param input the input stream to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoader.Builder.addStreamSource(input: InputStream, ext: String) = addSource(
  InputStreamPropertySource(input, ext)
)

/**
 * Returns a [PropertySource] that read the specified map values.
 *
 * @param map map
 */
fun ConfigLoader.Builder.addMapSource(map: Map<String, Any>) = addSource(
  MapPropertySource(map)
)

/**
 * Returns a [PropertySource] that will read the specified command line arguments.
 *
 * @param arguments command line arguments as passed to main method
 * @param prefix argument prefix
 * @param delimiter key value delimiter
 */
fun ConfigLoader.Builder.addCommandLineSource(
  arguments: Array<String>,
  prefix: String = "--",
  delimiter: String = "=",
) = addSource(
  CommandLinePropertySource(arguments, prefix, delimiter)
)

/**
 * Returns a [PropertySource] that will read the environment settings.
 *
 * @param arguments command line arguments as passed to main method
 * @param prefix argument prefix
 * @param delimiter key value delimiter
 */
fun ConfigLoader.Builder.addEnvironmentSource(
  useUnderscoresAsSeparator: Boolean = true,
  allowUppercaseNames: Boolean = true,
) = addSource(
  EnvironmentVariablesPropertySource(useUnderscoresAsSeparator, allowUppercaseNames)
)

/**
 * Returns a [PropertySource] that will read from the specified string.
 *
 * @param str the string to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoader.Builder.string(
  str: String, ext: String
) = addStreamSource(str.byteInputStream(), ext)
