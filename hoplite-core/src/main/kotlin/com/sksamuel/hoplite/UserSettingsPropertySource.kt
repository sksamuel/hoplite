package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import java.nio.file.Path
import java.nio.file.Paths

/**
 * An implementation of [PropertySource] that provides config through a config file
 * defined at ~/.userconfig.ext
 *
 * This file must use either the java properties format, or another format that you
 * have included the correct module for.
 *
 * Eg, if you have included hoplite-yaml module in your build, then your file can be
 * ~/.userconfig.yaml
 */
object UserSettingsPropertySource : PropertySource {

  private fun path(ext: String): Path = Paths.get(System.getProperty("user.home")).resolve(".userconfig.$ext")

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val ext = context.parsers.registeredExtensions().firstOrNull {
      path(it).toFile().exists()
    }
    return if (ext == null) Undefined.valid() else {
      val path = path(ext)
      val input = path.toFile().inputStream()
      context.parsers.locate(ext).map {
        it.load(input, path.toString())
      }
    }
  }
}
