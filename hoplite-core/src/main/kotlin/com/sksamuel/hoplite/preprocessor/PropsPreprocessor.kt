package com.sksamuel.hoplite.preprocessor

import java.io.InputStream
import java.nio.file.Path
import java.util.*

/**
 * Loads properties from a file and replaces strings of the form ${key} with the
 * value of that key in the properties file.
 *
 * When creating this class, you can specify the location of the properties file
 * either as a [Path] or as a resource on the classpath.
 */
class PropsPreprocessor(private val input: InputStream) : Preprocessor {

  private val regex = "\\$\\{(.*?)}".toRegex()

  private val props = Properties().apply {
    input.use {
      this.load(input)
    }
  }

  override fun process(value: String): String = regex.replace(value) {
    val key = it.groupValues[1]
    props[key]?.toString() ?: it.value
  }

  companion object {
    operator fun invoke(resource: String) = PropsPreprocessor(this::class.java.getResourceAsStream(resource))
    operator fun invoke(path: Path) = PropsPreprocessor(path.toFile().inputStream())
  }
}
