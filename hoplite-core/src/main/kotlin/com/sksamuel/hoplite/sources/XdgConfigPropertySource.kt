package com.sksamuel.hoplite.sources

import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.fp.valid
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream

/**
 * An implementation of [PropertySource] that provides config through a config file
 * defined at $XDG_CONFIG_HOME/.userconfig.ext
 *
 * This file must use either the java properties format, or another format that you
 * have included the correct module for.
 *
 * Eg, if you have included hoplite-yaml module in your build, then your file can be
 * $XDG_CONFIG_HOME/.userconfig.yaml
 */
object XdgConfigPropertySource : PropertySource {

  override fun source(): String = "\$XDG_CONFIG_HOME/hoplite.<ext>"

  private fun path(ext: String): Path? {
    val xdg = System.getenv("XDG_CONFIG_HOME")
    return if (xdg.isNullOrBlank()) null else Paths.get(xdg).resolve("hoplite.$ext")
  }

  override fun node(context: PropertySourceContext): ConfigResult<Node> {
    val path = context.parsers.registeredExtensions().firstNotNullOfOrNull { ext ->
      path(ext).takeIf { it?.exists() ?: false }
    }
    return if (path == null) Undefined.valid() else {
      val input = path.inputStream()
      context.parsers.locate(path.extension).map {
        it.load(input, path.toString())
      }
    }
  }
}
