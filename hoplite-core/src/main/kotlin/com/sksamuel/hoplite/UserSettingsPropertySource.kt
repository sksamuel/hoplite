package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

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

  override fun source(): String = System.getProperty("user.home") + "/.userconfig.<ext>"

  private fun path(ext: String): Path = Paths.get(System.getProperty("user.home")).resolve(".userconfig.$ext")

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val ext = context.parsers.registeredExtensions().firstOrNull {
      path(it).exists()
    }
    return if (ext == null) Undefined.valid() else {
      val path = path(ext)
      val input = path.inputStream()
      context.parsers.locate(ext).map {
        it.load(input, path.toString())
      }
    }
  }
}
