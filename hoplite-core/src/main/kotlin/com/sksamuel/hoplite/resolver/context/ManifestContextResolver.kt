package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.util.jar.Manifest

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
