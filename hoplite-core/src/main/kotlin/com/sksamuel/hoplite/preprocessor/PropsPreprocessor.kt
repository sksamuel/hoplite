package com.sksamuel.hoplite.preprocessor

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Loads properties from a file and replaces strings of the form ${key} with the
 * value of that key in the properties file.
 *
 * When creating this class, you can specify the location of the properties file
 * either as a [Path] or as a resource on the classpath.
 */
class PropsPreprocessor(private val path: Path) : Preprocessor {

  private val regex = "\\$\\{(.*?)}".toRegex()

  private val props = Properties().apply {
    this.load(Files.newInputStream(path))
  }

  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    props[key]?.toString() ?: it.value
  }

  companion object {
    operator fun invoke(resource: String) = PropsPreprocessor(Paths.get(javaClass.getResource(resource).path))
  }
}
