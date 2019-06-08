package com.sksamuel.hoplite

import arrow.data.ValidatedNel
import arrow.data.invalidNel
import java.net.URL

interface ConfigFailure {
  /**
   * A human-readable description of the failure.
   */
  fun description(): String

  /**
   * The optional location of the failure.
   */
  fun location(): ConfigLocation?

  companion object {
    operator fun invoke(description: String): ConfigFailure = GenericFailure(description)
  }
}

/**
 * A failure occurred because an exception was thrown during the reading process.
 *
 * @param throwable the exception thrown
 * @param location the optional location of the failure
 */
data class ThrowableFailure(val throwable: Throwable, val location: ConfigLocation?) : ConfigFailure {
  override fun description() = "${throwable.message}."
  override fun location(): ConfigLocation? = location
}

data class GenericFailure(val description: String) : ConfigFailure {
  override fun description(): String = description
  override fun location(): ConfigLocation? = null
}

/**
 * A failure occurred when converting from a `ConfigValue` to a given type. The failure contains a path to the
 * `ConfigValue` that raised the error.
 *
 * @param reason the reason for the conversion failure
 * @param location the optional location of the failure
 * @param path the path to the `ConfigValue` that raised the error
 * @param value the value that was requested to be converted
 * @param toType the target type that the value was requested to be converted to
 * @param because the reason why the conversion was not possible
 */
data class ConversionFailure(private val value: String,
                             private val toType: String,
                             private val because: String,
                             private val location: ConfigLocation?) : ConfigFailure {
  override fun description() = "Cannot convert '$value' to $toType: $because."
  override fun location(): ConfigLocation? = location
}

typealias ConfigResult<A> = ValidatedNel<ConfigFailure, A>

object ConfigResults {
  fun failed(description: String): ConfigResult<Nothing> = ConfigFailure(description).invalidNel()
}

/**
 * The file system location of a ConfigValue, represented by a url and a line
 * number
 *
 * @param url the URL describing the origin of the ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
data class ConfigLocation(val url: URL, val lineNumber: Int) {
  val description: String = "($url:$lineNumber)"
}

inline fun <reified A> loadConfig(): ConfigResult<A> = loadConfig("")

inline fun <reified A> loadConfig(namespace: String): ConfigResult<A> {
  //ConfigFactory.invalidateCaches()
  //val rawConfig = ConfigFactory.load()
  TODO()
}

