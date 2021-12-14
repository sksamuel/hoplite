package com.sksamuel.hoplite

import com.sksamuel.hoplite.parsers.JavaPropertiesParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropsParserTest : StringSpec() {
  init {
    "parse java style properties files" {
      JavaPropertiesParser().load(javaClass.getResourceAsStream("/basic.props"), source = "a.props") shouldBe
        MapNode(
          mapOf(
            "a" to MapNode(
              mapOf(
                "b" to MapNode(
                  map = mapOf(
                    "c" to StringNode(value = "wibble", pos = Pos.SourceNamePos(source = "a.props")),
                    "d" to StringNode(value = "123", pos = Pos.SourceNamePos(source = "a.props"))
                  ),
                  pos = Pos.SourceNamePos(source = "a.props"),
                  value = StringNode("qqq", pos = Pos.SourceNamePos(source = "a.props"))
                ),
                "g" to StringNode(value = "true", pos = Pos.SourceNamePos(source = "a.props"))
              ),
              pos = Pos.SourceNamePos(source = "a.props")
            ),
            "e" to StringNode(value = "5.5", pos = Pos.SourceNamePos(source = "a.props"))
          ),
          pos = Pos.SourceNamePos(source = "a.props")
        )
    }

    "empty properties load to an empty map" {
      JavaPropertiesParser().load(javaClass.getResourceAsStream("/empty.props"), source = "a.props") shouldBe
        MapNode(
          emptyMap(),
          pos = Pos.SourceNamePos("a.props")
        )
    }
  }
}
