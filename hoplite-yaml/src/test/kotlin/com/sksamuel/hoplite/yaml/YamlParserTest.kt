package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.StringNode
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import io.kotlintest.shouldBe

class YamlParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      YamlParser().load(javaClass.getResourceAsStream("/basic.yml"), "basic.yml") shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to StringNode(value = "1",
              pos = LineColPos(line = 1, col = 5, source = "basic.yml"),
              dotpath = "<root>.b"),
            "c" to StringNode(value = "true",
              pos = LineColPos(line = 2, col = 5, source = "basic.yml"),
              dotpath = "<root>.c"),
            "d" to StringNode(value = "2.3",
              pos = LineColPos(line = 3, col = 5, source = "basic.yml"),
              dotpath = "<root>.d")
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml"),
          dotpath = "<root>"
        )
    }

    test("parsing null fields") {
      YamlParser().load(javaClass.getResourceAsStream("/nulls.yml"), source = "basic.yml") shouldBe
        MapNode(
          map = mapOf(
            "a" to NullNode(pos = LineColPos(line = 0, col = 3, source = "basic.yml"), dotpath = "<root>.a"),
            "b" to MapNode(
              map = mapOf(
                "c" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 5, source = "basic.yml"),
                  dotpath = "<root>.b.c"),
                "d" to NullNode(pos = LineColPos(line = 3, col = 5, source = "basic.yml"), dotpath = "<root>.b.d")
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml"),
              dotpath = "<root>.b"
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml"),
          dotpath = "<root>"
        )
    }

    test("parsing nested basic arrays") {
      YamlParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.yml"), "basic.yml") shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to ListNode(
              elements = listOf(
                StringNode(value = "x", pos = LineColPos(line = 2, col = 4, source = "basic.yml"), dotpath = "<root>.b[0]"),
                StringNode(value = "y", pos = LineColPos(line = 3, col = 4, source = "basic.yml"), dotpath = "<root>.b[1]"),
                StringNode(value = "z", pos = LineColPos(line = 4, col = 4, source = "basic.yml"), dotpath = "<root>.b[2]")
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml"),
              dotpath = "<root>.b"
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml"),
          dotpath = "<root>"
        )
    }

    test("parsing nested container arrays") {
      YamlParser().load(
        javaClass.getResourceAsStream("/nested_container_arrays.yml"),
        source = "basic.yml"
      ) shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to ListNode(
              elements = listOf(
                MapNode(
                  map = mapOf(
                    "c" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[0].c"),
                    "d" to StringNode(value = "true", pos = LineColPos(line = 3, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[0].d")
                  ),
                  pos = LineColPos(line = 2, col = 4, source = "basic.yml"),
                  dotpath = "<root>.b[0]"
                ),
                MapNode(
                  map = mapOf(
                    "e" to StringNode(value = "1.4",
                      pos = LineColPos(line = 4, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[1].e"),
                    "f" to StringNode(value = "4",
                      pos = LineColPos(line = 5, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[1].f")
                  ),
                  pos = LineColPos(line = 4, col = 4, source = "basic.yml"),
                  dotpath = "<root>.b[1]"
                )
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml"),
              dotpath = "<root>.b"
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml"),
          dotpath = "<root>"
        )
    }
  }
}
