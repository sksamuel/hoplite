package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.PropsParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.FilterInputStream
import java.io.InputStream

class PropsParserTest : StringSpec() {
  init {
    "parse java style properties files" {
      PropsParser().load(javaClass.getResourceAsStream("/basic.props"), source = "a.props") shouldBe
        MapNode(
          mapOf(
            "a" to MapNode(
              mapOf(
                "b" to MapNode(
                  map = mapOf(
                    "c" to StringNode(
                      value = "wibble",
                      pos = Pos.SourcePos(source = "a.props"),
                      DotPath("a", "b", "c"),
                      sourceKey = "a.b.c"
                    ),
                    "d" to StringNode(value = "123", pos = Pos.SourcePos(source = "a.props"), DotPath("a", "b", "d"), sourceKey = "a.b.d")
                  ),
                  pos = Pos.SourcePos(source = "a.props"),
                  DotPath("a", "b"),
                  value = StringNode("qqq", pos = Pos.SourcePos(source = "a.props"), DotPath("a", "b"), sourceKey = "a.b"),
                  sourceKey = "a.b"
                ),
                "g" to StringNode(value = "true", pos = Pos.SourcePos(source = "a.props"), DotPath("a", "g"), sourceKey = "a.g")
              ),
              pos = Pos.SourcePos(source = "a.props"),
              DotPath("a"),
              sourceKey = "a"
            ),
            "e" to StringNode(value = "5.5", pos = Pos.SourcePos(source = "a.props"), DotPath("e"), sourceKey = "e")
          ),
          pos = Pos.SourcePos(source = "a.props"),
          DotPath.root
        )
    }

    "empty properties load to an empty map" {
      PropsParser().load(javaClass.getResourceAsStream("/empty.props"), source = "a.props") shouldBe
        MapNode(
          emptyMap(),
          pos = Pos.SourcePos("a.props"),
          DotPath.root
        )
    }

    // Regression: PropsParser used to wrap the caller's InputStream in an InputStreamReader
    // without closing it, leaking the decoder buffers (and a reference to the underlying
    // stream) until GC. The reader now lives inside a `.use {}` block, so closing the reader
    // closes its underlying InputStream too.
    "load should close the wrapped reader, propagating close() to the underlying input" {
      val tracking = CloseTrackingInputStream("a.b=c".byteInputStream())
      PropsParser().load(tracking, source = "a.props")
      tracking.closeCount shouldBe 1
    }
  }
}

private class CloseTrackingInputStream(delegate: InputStream) : FilterInputStream(delegate) {
  var closeCount: Int = 0
    private set

  override fun close() {
    closeCount++
    super.close()
  }
}
