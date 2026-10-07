package com.sksamuel.hoplite.parsers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.io.InputStream

class PropsParserStreamCloseTest : FunSpec({

  // Regression for #567. PropsParser previously wrapped `input` in InputStreamReader without
  // closing it, leaving the decoder buffers (and a reference to the underlying stream) on
  // the heap until GC. Closing the InputStreamReader closes the underlying stream too — so
  // wrapping the caller's stream in a tracker and asserting it's closed after load() returns
  // covers both halves.
  test("PropsParser closes the underlying input stream after parsing") {
    val tracker = TrackingInputStream("name=hoplite\nport=8080\n".byteInputStream())

    PropsParser().load(tracker, "test.props")

    tracker.closed shouldBe true
  }
})

private class TrackingInputStream(private val delegate: InputStream) : InputStream() {
  @Volatile var closed: Boolean = false
    private set
  override fun read(): Int = if (closed) throw IOException("Stream closed") else delegate.read()
  override fun read(b: ByteArray, off: Int, len: Int): Int =
    if (closed) throw IOException("Stream closed") else delegate.read(b, off, len)
  override fun close() {
    closed = true
    delegate.close()
  }
}
