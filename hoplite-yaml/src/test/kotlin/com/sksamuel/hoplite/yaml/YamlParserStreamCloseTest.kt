package com.sksamuel.hoplite.yaml

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class YamlParserStreamCloseTest : FunSpec({

  // Regression: YamlParser previously wrapped `input` in InputStreamReader without closing
  // it (#561). Closing the InputStreamReader closes the underlying stream too — so wrapping
  // the caller's stream in a tracker and asserting it's closed after parse() returns covers
  // both halves.
  test("YamlParser closes the underlying input stream after parsing") {
    val tracker = TrackingInputStream("name: hoplite\nport: 8080\n".byteInputStream())

    YamlParser().load(tracker, "test.yaml")

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
