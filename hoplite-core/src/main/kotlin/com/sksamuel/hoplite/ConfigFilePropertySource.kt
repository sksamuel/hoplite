package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatRecover
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
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
 */
class ConfigFilePropertySource(
  private val config: ConfigSource,
  private val optional: Boolean = false,
) : PropertySource {

  override fun source(): String = "Config File"

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val parser = context.parsers.locate(config.ext())
    return Validated.ap(parser, config.open()) { a, b -> b.use { a.load(it, config.describe()) } }
      .mapInvalid { ConfigFailure.MultipleFailures(it) }
      .flatRecover { if (optional) Undefined.valid() else it.invalid() }
  }

  companion object {

    fun optionalPath(
       path: Path,
    ): ConfigFilePropertySource =
       ConfigFilePropertySource(ConfigSource.PathSource(path), true)

    fun optionalFile(
      file: File,
    ): ConfigFilePropertySource = optionalPath(file.toPath())

    fun optionalResource(
      resource: String,
    ): ConfigFilePropertySource =
       ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), true)
  }
}
