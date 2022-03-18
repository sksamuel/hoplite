package com.sksamuel.hoplite

import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

abstract class ConfigSource {

  /**
   * Opens a new [InputStream], the caller should call [AutoCloseable.close]
   * when they no longer need the [InputStream].
   */
  abstract fun open(): ConfigResult<InputStream>

  /**
   * Return a string detailing the location of this source, eg file://myfile
   */
  abstract fun describe(): String

  /**
   * Returns the file extension associated with this config source.
   */
  abstract fun ext(): String

  class PathSource(val path: Path) : ConfigSource() {
    override fun describe(): String = path.toString()
    override fun ext() = path.fileName.toString().split('.').last()
    override fun open(): ConfigResult<InputStream> =
      runCatching { Files.newInputStream(path) }
        .toValidated { ConfigFailure.UnknownSource(path.toString()) }
  }

  class ClasspathSource(
    private val resource: String,
    private val classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
  ) : ConfigSource() {
    override fun describe(): String = resource
    override fun ext() = resource.split('.').last()
    override fun open(): ConfigResult<InputStream> =
      classpathResourceLoader.getResourceAsStream(resource)?.valid() ?: ConfigFailure.UnknownSource(resource).invalid()
  }

  companion object {

    /**
     * If this [resourceOrFile] is located in the classpath returns a [ConfigSource.ClasspathSource],
     * otherwise if this [resourceOrFile] is located in the filesystem returns a [ConfigSource.PathSource].
     * If the resource is neither on the classpath nor the fileystem, returns a [ConfigFailure].
     */
    fun invoke(
      resourceOrFile: String,
      classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
    ): ConfigResult<ConfigSource> {
      val path = Paths.get(resourceOrFile)
      return if (path.exists()) {
        PathSource(path).valid()
      } else {
        classpathResourceLoader.getResourceAsStream(resourceOrFile)
          ?.let { ClasspathSource(resourceOrFile, classpathResourceLoader).valid() }
          ?: ConfigFailure.UnknownSource(resourceOrFile).invalid()
      }
    }

    fun fromClasspathResources(
      resources: List<String>,
      classpathResourceLoader: ClasspathResourceLoader = Companion::class.java.toClasspathResourceLoader(),
    ): ConfigResult<List<ConfigSource>> {
      return resources.map { resource ->
        classpathResourceLoader.getResourceAsStream(resource)
          ?.let { ClasspathSource(resource, classpathResourceLoader).valid() }
          ?: ConfigFailure.UnknownSource(resource).invalid()
      }.sequence()
        .mapInvalid { ConfigFailure.MultipleFailures(it) }
    }

    fun fromPaths(paths: List<Path>): ConfigResult<List<ConfigSource>> {
      return paths.map { path ->
        runCatching { Files.newInputStream(path) }.fold(
          { PathSource(path).valid() },
          { ConfigFailure.UnknownSource(path.toString()).invalid() },
        )
      }.sequence()
        .mapInvalid { ConfigFailure.MultipleFailures(it) }
    }

    fun fromFiles(files: List<File>): ConfigResult<List<ConfigSource>> {
      return fromPaths(files.map { it.toPath() })
    }
  }
}
