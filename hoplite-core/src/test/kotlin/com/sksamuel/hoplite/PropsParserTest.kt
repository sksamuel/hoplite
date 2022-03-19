package com.sksamuel.hoplite

import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.parsers.PropsParser
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

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
                    "c" to StringNode(value = "wibble", pos = Pos.FilePos(source = "a.props"), DotPath.root),
                    "d" to StringNode(value = "123", pos = Pos.FilePos(source = "a.props"), DotPath.root)
                  ),
                  pos = Pos.FilePos(source = "a.props"),
                  DotPath.root,
                  value = StringNode("qqq", pos = Pos.FilePos(source = "a.props"), DotPath.root)
                ),
                "g" to StringNode(value = "true", pos = Pos.FilePos(source = "a.props"), DotPath.root)
              ),
              pos = Pos.FilePos(source = "a.props"),
              DotPath.root
            ),
            "e" to StringNode(value = "5.5", pos = Pos.FilePos(source = "a.props"), DotPath.root)
          ),
          pos = Pos.FilePos(source = "a.props"),
          DotPath.root
        )
    }

    "empty properties load to an empty map" {
      PropsParser().load(javaClass.getResourceAsStream("/empty.props"), source = "a.props") shouldBe
        MapNode(
          emptyMap(),
          pos = Pos.FilePos("a.props"),
          DotPath.root,
        )
    }
  }
}
