package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatRecover
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.io.File
import java.nio.file.Path

/**
 * An implementation of [PropertySource] that loads values from a file located
 * via a [ConfigSource]. The file is parsed using an instance of [Parser] retrieved
 * from the [ParserRegistry] based on file extension.
 *
 * @param optional if true then if a file is missing, this property source will be skipped. If false, then a missing
 * file will cause the config to fail. Defaults to false.
 */
class ConfigFilePropertySource(
   private val config: ConfigSource,
   private val optional: Boolean = false
) : PropertySource {

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val parser = context.parsers.locate(config.ext())
    val input = config.open()
    return Validated.ap(parser, input) { a, b -> a.load(b, config.describe()) }
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
    ): ConfigFilePropertySource =
       ConfigFilePropertySource(ConfigSource.FileSource(file), true)

    fun optionalResource(
      resource: String,
    ): ConfigFilePropertySource =
       ConfigFilePropertySource(ConfigSource.ClasspathSource(resource), true)
  }
}
