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

  override fun source(): String = System.getenv("XDG_CONFIG_HOME") + "/hoplite.<ext>"

  private fun path(ext: String): Path = Paths.get(System.getProperty("XDG_CONFIG_HOME")).resolve("hoplite.$ext")

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
