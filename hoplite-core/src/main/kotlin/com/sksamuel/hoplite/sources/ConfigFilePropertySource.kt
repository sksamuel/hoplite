package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.toValidated
import java.io.File
import java.nio.file.Path

/**
 * A [PropertySource] that loads values from a config value, provided by a [ConfigSource].
 *
 * The file is parsed using a [Parser] that is retrieved from the [ParserRegistry]
 * based on file extension.
 *
 * @param optional if true then a missing file will be skipped.
 *                 if false, then a missing file will return an error.
 *                 Defaults to false.
 *
 * @param allowEmpty if true then the config file can be empty
 */
class ConfigFilePropertySource(
  private val config: ConfigSource,
  private val optional: Boolean = false,
  private val allowEmpty: Boolean,
) : PropertySource {

  /**
   * Return a string detailing the location of this source, eg file://myfile
   */
  override fun source(): String = config.describe()

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    return context.parsers.locate(config.ext()).flatMap { parser ->
      config.open(optional).flatMap { input ->
        when {
          input == null && optional -> Undefined.valid()
          input == null -> ConfigFailure.UnknownSource(config.describe()).invalid()
          // Wrap every branch that touches `input` in `.use {}` so the stream is closed on
          // every path — including the empty-file branches, which previously leaked the
          // stream when allowEmpty was true (Undefined) or false (EmptyConfigSource).
          else -> input.use {
            when {
              it.available() == 0 && (allowEmpty || context.allowEmptyConfigFiles) -> Undefined.valid()
              it.available() == 0 -> ConfigFailure.EmptyConfigSource(config).invalid()
              else -> runCatching {
                parser.load(it, config.describe())
              }.toValidated { ex -> ConfigFailure.PropertySourceFailure("Could not parse ${config.describe()}", ex) }
            }
          }
        }
      }
    }
  }

  companion object {

    fun optionalPath(
      path: Path
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.PathSource(path), optional = true, allowEmpty = false)

    fun optionalFile(
      file: File
    ): ConfigFilePropertySource = optionalPath(file.toPath())

    fun optionalResource(
      resource: String
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = true, allowEmpty = false)
  }
}
