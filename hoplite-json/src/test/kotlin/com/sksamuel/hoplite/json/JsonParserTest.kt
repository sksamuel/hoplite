package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import io.kotlintest.shouldBe

class JsonParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      JsonParser().load(javaClass.getResourceAsStream("/basic.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(Value.StringNode(value = "hello"), pos = LineColPos(line = 2, col = 15, source = "a.json")),
            "b" to PrimitiveNode(Value.LongNode(value = 1), pos = LineColPos(line = 3, col = 9, source = "a.json")),
            "c" to PrimitiveNode(Value.BooleanNode(value = true), pos = LineColPos(line = 4, col = 12, source = "a.json")),
            "d" to PrimitiveNode(Value.DoubleNode(value = 2.3), pos = LineColPos(line = 5, col = 11, source = "a.json"))
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json")
        )
    }

    test("parsing basic json with comments") {
      JsonParser().load(javaClass.getResourceAsStream("/basic_with_comments.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(Value.StringNode(value = "hello"), pos = LineColPos(line = 3, col = 15, source = "a.json")),
            "b" to PrimitiveNode(Value.LongNode(value = 1), pos = LineColPos(line = 5, col = 9, source = "a.json")),
            "c" to PrimitiveNode(Value.BooleanNode(value = true), pos = LineColPos(line = 7, col = 12, source = "a.json")),
            "d" to PrimitiveNode(Value.DoubleNode(value = 2.3), pos = LineColPos(line = 9, col = 11, source = "a.json"))
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json")
        )
    }

    test("parsing nested basic arrays") {
      JsonParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(Value.StringNode(value = "hello"),
              pos = LineColPos(line = 2, col = 15, source = "a.json")
            ),
            "b" to ArrayNode(
              elements = listOf(
                PrimitiveNode(Value.StringNode(value = "x"), pos = LineColPos(line = 4, col = 8, source = "a.json")),
                PrimitiveNode(Value.StringNode(value = "y"), pos = LineColPos(line = 5, col = 8, source = "a.json")),
                PrimitiveNode(Value.StringNode(value = "z"), pos = LineColPos(line = 6, col = 8, source = "a.json"))
              ),
              pos = LineColPos(line = 3, col = 9, source = "a.json")
            )
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json")
          )
    }

    test("parsing nested container arrays") {
      JsonParser().load(javaClass.getResourceAsStream("/nested_container_arrays.json"), source = "a.json") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(Value.StringNode(value = "hello"),
              pos = LineColPos(line = 2, col = 15, source = "a.json")
            ),
            "b" to ArrayNode(
              elements = listOf(
                MapNode(
                  map = mapOf(
                    "c" to PrimitiveNode(Value.StringNode(value = "hello"), pos = LineColPos(line = 5, col = 19, source = "a.json")),
                    "d" to PrimitiveNode(Value.BooleanNode(value = true), pos = LineColPos(line = 6, col = 16, source = "a.json"))
                  ),
                  pos = LineColPos(line = 4, col = 6, source = "a.json")
                ),
                MapNode(
                  map = mapOf(
                    "e" to PrimitiveNode(Value.DoubleNode(value = 1.4), pos = LineColPos(line = 9, col = 15, source = "a.json")),
                    "f" to PrimitiveNode(Value.LongNode(value = 4), pos = LineColPos(line = 10, col = 13, source = "a.json"))
                  ),
                  pos = LineColPos(line = 8, col = 6, source = "a.json")
                )
              ),
              pos = LineColPos(line = 3, col = 9, source = "a.json")
            )
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json")
        )
    }
  }
}
