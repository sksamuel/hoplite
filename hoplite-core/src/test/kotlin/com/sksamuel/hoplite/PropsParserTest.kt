package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.PropsParser
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

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
                    "c" to StringNode(value = "wibble", pos = Pos.FilePos(source = "a.props")),
                    "d" to StringNode(value = "123", pos = Pos.FilePos(source = "a.props"))
                  ),
                  pos = Pos.FilePos(source = "a.props")
                ),
                "d" to StringNode(value = "true", pos = Pos.FilePos(source = "a.props"))
              ),
              pos = Pos.FilePos(source = "a.props")
            ),
            "e" to StringNode(value = "5.5", pos = Pos.FilePos(source = "a.props"))
          ),
          pos = Pos.FilePos(source = "a.props")
        )
    }
  }
}
