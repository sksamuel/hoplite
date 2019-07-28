package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.StringNode
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import io.kotlintest.shouldBe

class JsonTest : FunSpec() {
  init {

    test("parsing basic json") {
      JsonParser().load(javaClass.getResourceAsStream("/basic.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to LongNode(value = 1, pos = LineColPos(line = 3, col = 9, source = "a.json"), dotpath = "<root>.b"),
            "c" to BooleanNode(value = true, pos = LineColPos(line = 4, col = 12, source = "a.json"),
              dotpath = "<root>.c"),
            "d" to DoubleNode(value = 2.3, pos = LineColPos(line = 5, col = 11, source = "a.json"),
              dotpath = "<root>.d")
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json"),
          dotpath = "<root>"
        )
    }

    test("parsing nested basic arrays") {
      JsonParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello",
              pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to ListNode(
              elements = listOf(
                StringNode(value = "x", pos = LineColPos(line = 4, col = 8, source = "a.json"), dotpath = "<root>.b[0]"),
                StringNode(value = "y", pos = LineColPos(line = 5, col = 8, source = "a.json"), dotpath = "<root>.b[1]"),
                StringNode(value = "z", pos = LineColPos(line = 6, col = 8, source = "a.json"), dotpath = "<root>.b[2]")
              ),
              pos = LineColPos(line = 3, col = 9, source = "a.json"),
              dotpath = "<root>.b"
            )
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json"),
          dotpath = "<root>"
          )
    }

    test("parsing nested container arrays") {
      JsonParser().load(javaClass.getResourceAsStream("/nested_container_arrays.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to StringNode(value = "hello",
              pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to ListNode(
              elements = listOf(
                MapNode(
                  map = mapOf(
                    "c" to StringNode(value = "hello", pos = LineColPos(line = 5, col = 19, source = "a.json"),
                      dotpath = "<root>.b[0].c"),
                    "d" to BooleanNode(value = true, pos = LineColPos(line = 6, col = 16, source = "a.json"),
                      dotpath = "<root>.b[0].d")
                  ),
                  pos = LineColPos(line = 4, col = 6, source = "a.json"),
                  dotpath = "<root>.b[0]"
                ),
                MapNode(
                  map = mapOf(
                    "e" to DoubleNode(value = 1.4, pos = LineColPos(line = 9, col = 15, source = "a.json"),
                      dotpath = "<root>.b[1].e"),
                    "f" to LongNode(value = 4, pos = LineColPos(line = 10, col = 13, source = "a.json"),
                      dotpath = "<root>.b[1].f")
                  ),
                  pos = LineColPos(line = 8, col = 6, source = "a.json"),
                  dotpath = "<root>.b[1]"
                )
              ),
              pos = LineColPos(line = 3, col = 9, source = "a.json"),
              dotpath = "<root>.b"
            )
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json"),
          dotpath = "<root>"
        )
    }
  }
}
