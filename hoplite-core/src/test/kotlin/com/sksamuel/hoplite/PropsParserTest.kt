package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.PropsParser
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PropsParserTest : StringSpec() {
  init {
    "parse java style properties files" {
      PropsParser().load(javaClass.getResourceAsStream("/basic.props"), source = "a.props") shouldBe
        MapValue(
          mapOf(
            "a" to MapValue(
              mapOf(
                "b" to MapValue(
                  map = mapOf(
                    "c" to StringValue(value = "wibble", pos = Pos.FilePos(source = "a.props"), dotpath = "<root>.a.b.c"),
                    "d" to StringValue(value = "123", pos = Pos.FilePos(source = "a.props"), dotpath = "<root>.a.b.d")
                  ),
                  pos = Pos.FilePos(source = "a.props"),
                  dotpath = "<root>.a.b"
                ),
                "d" to StringValue(value = "true", pos = Pos.FilePos(source = "a.props"), dotpath = "<root>.a.d")
              ),
              pos = Pos.FilePos(source = "a.props"),
              dotpath = "<root>.a"
            ),
            "e" to StringValue(value = "5.5", pos = Pos.FilePos(source = "a.props"), dotpath = "<root>.e")
          ),
          pos = Pos.FilePos(source = "a.props"),
          dotpath = "<root>"
        )
    }
  }
}
