package com.sksamuel.hoplite

import arrow.core.Try
import arrow.core.toOption
import arrow.data.Valid
import arrow.data.invalid
import arrow.data.valid
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.arrow.toValidated
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

sealed class FileSource {

  abstract fun open(): ConfigResult<InputStream>
  abstract fun describe(): String
  abstract fun ext(): String

  class PathSource(val path: Path) : FileSource() {
    override fun describe(): String = path.toString()
    override fun ext() = path.fileName.toString().split('.').last()
    override fun open(): ConfigResult<InputStream> =
      Try { Files.newInputStream(path) }
        .toValidated { ConfigFailure.UnknownSource(path.toString()) }
  }

  class ClasspathSource(private val resource: String) : FileSource() {
    override fun describe(): String = resource
    override fun ext() = resource.split('.').last()
    override fun open(): ConfigResult<InputStream> =
      this.javaClass.getResourceAsStream(resource).toOption().fold(
        { ConfigFailure.UnknownSource(resource).invalid() }, { Valid(it) }
      )
  }

  companion object {
    fun fromClasspathResources(resources: List<String>): ConfigResult<List<FileSource>> {
      return resources.map { resource ->
        this::class.java.getResourceAsStream(resource).toOption().fold(
          { ConfigFailure.UnknownSource(resource).invalid() },
          { ClasspathSource(resource).valid() }
        )
      }.sequence()
        .leftMap { ConfigFailure.MultipleFailures(it) }
    }

    fun fromPaths(paths: List<Path>): ConfigResult<List<FileSource>> {
      return paths.map { path ->
        Try { Files.newInputStream(path) }.fold(
          { ConfigFailure.UnknownSource(path.toString()).invalid() },
          { PathSource(path).valid() }
        )
      }.sequence()
        .leftMap { ConfigFailure.MultipleFailures(it) }
    }
  }
}
