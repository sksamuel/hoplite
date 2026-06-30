package com.sksamuel.hoplite.json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.io.InputStream

class JsonParserStreamCloseTest : FunSpec({

  // Regression: JsonParser previously created a Jackson JsonParser around `input` without
  // closing it (#561). Jackson's parser owns the stream, and closing the parser closes the
  // underlying stream — so wrapping the caller's stream in a tracker and asserting it's
  // closed after load() returns covers both halves.
  test("JsonParser closes the underlying input stream after parsing") {
    val tracker = TrackingInputStream("""{"name": "hoplite", "port": 8080}""".byteInputStream())

    JsonParser().load(tracker, "test.json")

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
