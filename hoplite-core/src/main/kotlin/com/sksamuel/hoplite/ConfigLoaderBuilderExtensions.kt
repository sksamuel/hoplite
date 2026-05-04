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
  // Probe the filesystem with both the verbatim path and the slash-stripped form so that:
  //  - absolute paths (`/etc/config.yaml`) resolve as files, not as relative-to-CWD that fall
  //    through to classpath lookup; and
  //  - `/foo` written as a classpath-style spec still finds a sibling `foo` relative to CWD,
  //    matching the existing behaviour exercised by SmarterPathAndClasspathLoadingTest.
  // The classpath fallback uses the slash-stripped form because a leading `/` is meaningless
  // for ClassLoader.getResourceAsStream.
  val verbatim = Paths.get(resourceOrFile)
  val stripped = Paths.get(resourceOrFile.removePrefix("/"))
  val filesystemPath = when {
    verbatim.exists() -> verbatim
    stripped.exists() -> stripped
    else -> null
  }
  return if (filesystemPath != null) {
    addPropertySource(
      ConfigFilePropertySource(
        ConfigSource.PathSource(filesystemPath),
        // Forward the user's `optional` flag. Without this, the file branch silently
        // ignored `optional = true`: if the file existed at probe time but disappeared
        // before load (TOCTOU), the loader threw instead of skipping the source as the
        // user requested. (See #571.)
        optional = optional,
        allowEmpty = allowEmpty
      )
    )
  } else {
    addPropertySource(
      ConfigFilePropertySource(
        ConfigSource.ClasspathSource(resourceOrFile.removePrefix("/")),
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
 */
fun ConfigLoaderBuilder.addEnvironmentSource() = addPropertySource(
  EnvironmentVariablesPropertySource()
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
