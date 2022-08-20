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
 * @param allowEmpty if true then the config file can beempty
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
          input.available() == 0 && allowEmpty -> Undefined.valid()
          input.available() == 0 -> ConfigFailure.EmptyConfigSource(config).invalid()
          else -> runCatching {
            input.use { parser.load(it, config.describe()) }
          }.toValidated { ConfigFailure.PropertySourceFailure("Could not parse ${config.describe()}") }
        }
      }
    }
  }

  companion object {

    fun optionalPath(
      path: Path,
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.PathSource(path), optional = true, allowEmpty = false)

    fun optionalFile(
      file: File,
    ): ConfigFilePropertySource = optionalPath(file.toPath())

    fun optionalResource(
      resource: String,
    ): ConfigFilePropertySource =
      ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), optional = true, allowEmpty = false)
  }
}
