package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.io.InputStream
import java.util.jar.Manifest

object ManifestContextResolver : ContextResolver() {

  override val contextKey: String = "manifest"
  override val default: Boolean = true

  // Visible for testing so the test can swap in a tracking InputStream. Production code
  // continues to load /META-INF/MANIFEST.MF from the resolver's own class loader.
  internal var streamProvider: () -> InputStream? =
    { javaClass.getResourceAsStream("/META-INF/MANIFEST.MF") }

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return runCatching {
      val input = streamProvider()
      if (input == null) ConfigFailure.ResolverFailure("Manifest could not be located").invalid() else {
        // Manifest(InputStream) reads from but does not close the stream — wrap in .use {} so we
        // don't leak a classpath-resource handle on every ${{ manifest:... }} resolve call.
        input.use { Manifest(it).mainAttributes.getValue(path) }.valid()
      }
    }.getOrElse {
       ConfigFailure.ResolverException("Error loading manifest", it).invalid()
    }
  }
}
