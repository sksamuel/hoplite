package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.CommandLinePropertySource
import com.sksamuel.hoplite.sources.ConfigFilePropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.InputStreamPropertySource
import com.sksamuel.hoplite.sources.MapPropertySource
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Adds a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param resource the resource to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addResourceSource(
  resource: String,
  optional: Boolean = false,
  allowEmpty: Boolean = false
) = addPropertySource(ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional, allowEmpty))

fun ConfigLoaderBuilder.addFileSource(
  file: String,
  optional: Boolean = false,
  allowEmpty: Boolean = false
): ConfigLoaderBuilder = addFileSource(File(file), optional, allowEmpty)

/**
 * Adds a [PropertySource] that will read the specified file from the filesystem.
 *
 * @param file the [File] to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addFileSource(
  file: File,
  optional: Boolean = false,
  allowEmpty: Boolean = false,
) = addPathSource(file.toPath(), optional, allowEmpty)

/**
 * Adds a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param path the [Path] of the resource to be read
 * @param optional if true, the config loader will ignore this source if the resource does not exist
 */
fun ConfigLoaderBuilder.addPathSource(
  path: Path,
  optional: Boolean = false,
  allowEmpty: Boolean = false,
) = addPropertySource(ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional, allowEmpty))

/**
 * Adds a [PropertySource] to this [ConfigLoaderBuilder] that will read the specified [resourceOrFile]
 * from either the classpath or the filesystem. If the provided [resourceOrFile] does not exist on the filesystem,
 * then this method will assume it is a classpath resource.
 *
 * @param resourceOrFile the classpath resource or filesystem file.
 * @param optional if true, the config loader will ignore this source if the resourceOrFile does not exist
 */
fun ConfigLoaderBuilder.addResourceOrFileSource(
  resourceOrFile: String,
  optional: Boolean = false,
  allowEmpty: Boolean = false,
): ConfigLoaderBuilder {
  val path = Paths.get(resourceOrFile)
  return if (path.exists()) {
    addPropertySource(
      ConfigFilePropertySource(
        ConfigSource.PathSource(path),
        allowEmpty = allowEmpty
      )
    )
  } else {
    addPropertySource(
      ConfigFilePropertySource(
        ConfigSource.ClasspathSource(resourceOrFile),
        optional,
        allowEmpty
      )
    )
  }
}

/**
 * Adds a [PropertySource] that will read the specified input stream.
 *
 * @param input the [InputStream] to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoaderBuilder.addStreamSource(input: InputStream, ext: String) = addPropertySource(
  InputStreamPropertySource(input, ext, "$ext input stream")
)

/**
 * Adds a [PropertySource] that will read the specified map values.
 *
 * @param map the [Map] to read from
 */
fun ConfigLoaderBuilder.addMapSource(map: Map<String, Any>) = addPropertySource(
  MapPropertySource(map)
)

/**
 * Adds a [PropertySource] that will read the specified command line arguments.
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
 * Adds a [PropertySource] that will read the environment settings.
 *
 * @param useUnderscoresAsSeparator if true, use double underscore instead of period to separate keys in nested config
 * @param allowUppercaseNames if true, allow uppercase-only names
 * @param useSingleUnderscoresAsSeparator if true, allows single underscores as separators, to conform with
 * idiomatic environment variable names
 */
fun ConfigLoaderBuilder.addEnvironmentSource(
  useUnderscoresAsSeparator: Boolean = true,
  allowUppercaseNames: Boolean = true,
  useSingleUnderscoresAsSeparator: Boolean = false,
) = addPropertySource(
  EnvironmentVariablesPropertySource(useUnderscoresAsSeparator, useSingleUnderscoresAsSeparator, allowUppercaseNames)
)

/**
 * Adds a [PropertySource] that will read the environment settings.
 *
 * With this source, environment variables are expected to be idiomatic i.e. uppercase, with underscores as
 * separators for path elements. Dashes are removed.
 *
 * Generally a [PathNormalizer] should be added to the [ConfigLoaderBuilder] to normalize paths when this source
 * is used.
 */
fun ConfigLoaderBuilder.addIdiomaticEnvironmentSource() = addPropertySource(
  EnvironmentVariablesPropertySource(
    useUnderscoresAsSeparator = false,
    useSingleUnderscoresAsSeparator = true,
    allowUppercaseNames = false
  )
)

/**
 * Adds a [PropertySource] that will read from the specified string.
 *
 * @param str the [String] to read from
 * @param ext the file extension of the input format
 */
fun ConfigLoaderBuilder.string(
  str: String, ext: String
) = addStreamSource(str.byteInputStream(), ext)
