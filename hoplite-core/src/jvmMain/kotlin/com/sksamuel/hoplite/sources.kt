package com.sksamuel.hoplite

import java.io.File
import java.io.InputStream
import java.nio.file.Path

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun PropertySource.Companion.resource(resource: String, optional: Boolean = false) =
  ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = optional)

/**
 * Returns a [PropertySource] that will read the specified file from the filesystem.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun PropertySource.Companion.file(file: File, optional: Boolean = false) =
  ConfigFilePropertySource(ConfigSource.FileSource(file), optional = optional)

/**
 * Returns a [PropertySource] that will read the specified resource from the classpath.
 *
 * @param optional if true then the resource can not exist and the config loader will ignore this source
 */
fun PropertySource.Companion.path(path: Path, optional: Boolean = false) =
  ConfigFilePropertySource(ConfigSource.PathSource(path), optional = optional)

/**
 * Returns a [PropertySource] that will read the specified input stream.
 *
 * @param input the input stream to read from
 * @param ext the file extension of the input format
 */
fun PropertySource.Companion.stream(input: InputStream, ext: String) =
  InputStreamPropertySource(input, ext)
