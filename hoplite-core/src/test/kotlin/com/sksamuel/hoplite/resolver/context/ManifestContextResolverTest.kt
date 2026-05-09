package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.decoder.defaultDecoderRegistry
import com.sksamuel.hoplite.defaultNodeTransformers
import com.sksamuel.hoplite.defaultParamMappers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference

class ManifestContextResolverTest : FunSpec({

  // Regression: Manifest(InputStream) reads from but does not close the stream. Without
  // the .use {} wrapper added in #555 every ${{ manifest:X }} lookup leaked one classpath-
  // resource handle, accumulating until GC. Verify the stream is closed after lookup
  // returns by reading from it afterwards (a closed FileInputStream / wrapped stream
  // throws IOException on read).

  val ctx = DecoderContext(
    decoders = defaultDecoderRegistry(),
    paramMappers = defaultParamMappers(),
    nodeTransformers = defaultNodeTransformers(),
  )

  // ByteArrayInputStream.close() is a no-op — read() works fine after close. Wrap in a
  // tracker that flips a flag on close so the test can observe it directly.
  class ClosableTracker(delegate: InputStream) : InputStream() {
    private val inner = delegate
    @Volatile var closed: Boolean = false
      private set
    override fun read() = if (closed) throw IOException("Stream closed") else inner.read()
    override fun read(b: ByteArray, off: Int, len: Int) =
      if (closed) throw IOException("Stream closed") else inner.read(b, off, len)
    override fun close() {
      closed = true
      inner.close()
    }
  }

  fun manifestStreamFor(implementationVersion: String): InputStream {
    val raw = "Manifest-Version: 1.0\nImplementation-Version: $implementationVersion\n\n"
    return ClosableTracker(ByteArrayInputStream(raw.toByteArray()))
  }

  test("ManifestContextResolver closes the input stream after reading the manifest") {
    val captured = AtomicReference<ClosableTracker>()
    ManifestContextResolver.streamProvider = {
      (manifestStreamFor("1.2.3") as ClosableTracker).also { captured.set(it) }
    }
    try {
      runBlocking {
        ManifestContextResolver
          .lookup("Implementation-Version", StringNode("ignored", Pos.NoPos, DotPath.root), Undefined, ctx)
      }

      // Without #555's `.use {}` wrapper the tracker would still be open.
      captured.get().closed shouldBe true
    } finally {
      // Restore default so other tests using the real /META-INF/MANIFEST.MF aren't disturbed.
      ManifestContextResolver.streamProvider = {
        ManifestContextResolver::class.java.getResourceAsStream("/META-INF/MANIFEST.MF")
      }
    }
  }
})
