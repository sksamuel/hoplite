package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.sequence
import com.sksamuel.hoplite.fp.valid
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

abstract class ConfigSource {

  abstract fun open(): ConfigResult<InputStream>
  abstract fun describe(): String
  abstract fun ext(): String

  class PathSource(val path: Path) : ConfigSource() {
    override fun describe(): String = path.toString()
    override fun ext() = path.fileName.toString().split('.').last()
    override fun open(): ConfigResult<InputStream> =
      runCatching { Files.newInputStream(path) }
        .toValidated { ConfigFailure.UnknownSource(path.toString()) }
  }

  class FileSource(val file: File) : ConfigSource() {
    override fun describe(): String = file.absolutePath
    override fun ext() = file.extension
    override fun open(): ConfigResult<InputStream> =
      runCatching { FileInputStream(file) }
        .toValidated { ConfigFailure.UnknownSource(file.absolutePath) }
  }

  class ClasspathSource(private val resource: String) : ConfigSource() {
    override fun describe(): String = resource
    override fun ext() = resource.split('.').last()
    override fun open(): ConfigResult<InputStream> =
      this.javaClass.getResourceAsStream(resource)?.valid() ?: ConfigFailure.UnknownSource(resource).invalid()
  }

  companion object {
    fun fromClasspathResources(resources: List<String>): ConfigResult<List<ConfigSource>> {
      return resources.map { resource ->
        this::class.java.getResource(resource)?.let { ClasspathSource(resource).valid() }
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
      return files.map { file ->
        FileSource(file).valid()
      }.sequence()
        .mapInvalid { ConfigFailure.MultipleFailures(it) }
    }
  }
}
