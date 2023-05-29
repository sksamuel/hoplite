@file:Suppress("RegExpRedundantEscape")

package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.util.jar.Manifest

/**
 * Replaces strings of the form ${{ hoplite:environment }} by using the [com.sksamuel.hoplite.env.Environment] value
 * provided to the config loader. Defaults can also be applied in case the environment does not
 * exist, eg: ${{ hoplite:environment :- default }}
 */
object HopliteContextResolver : ContextResolver() {

  override val contextKey: String = "hoplite"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return context.environment?.name.valid()
  }
}

object SystemContextResolver : ContextResolver() {

  override val contextKey: String = "system"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return when (path) {
      "processors" -> Runtime.getRuntime().availableProcessors().toString().valid()
      else -> ConfigFailure.ResolverFailure("Uknown system context path $path").invalid()
    }
  }
}

object ManifestContextResolver : ContextResolver() {

  override val contextKey: String = "manifest"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return runCatching {
      val input = javaClass.getResourceAsStream("/META-INF/MANIFEST.MF")
      if (input == null) ConfigFailure.ResolverFailure("Manifest could not be located").invalid() else {
        val manifest = Manifest(input)
        manifest.mainAttributes.getValue(path).valid()
      }
    }.getOrElse {
      ConfigFailure.ResolverException("Error loading manifest", it).invalid()
    }
  }
}
