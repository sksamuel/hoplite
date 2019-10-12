package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import io.kotlintest.shouldBe

class YamlParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      YamlParser().load(javaClass.getResourceAsStream("/basic.yml"), "basic.yml") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(
              Value.StringNode(value = "hello"),
              pos = LineColPos(line = 0, col = 5, source = "basic.yml")
            ),
            "b" to PrimitiveNode(
              Value.StringNode(value = "1"),
              pos = LineColPos(line = 1, col = 5, source = "basic.yml")
            ),
            "c" to PrimitiveNode(
              Value.StringNode(value = "true"),
              pos = LineColPos(line = 2, col = 5, source = "basic.yml")
            ),
            "d" to PrimitiveNode(
              Value.StringNode(value = "2.3"),
              pos = LineColPos(line = 3, col = 5, source = "basic.yml")
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml")
        )
    }

    test("parsing null fields") {
      YamlParser().load(javaClass.getResourceAsStream("/nulls.yml"), source = "basic.yml") shouldBe
        MapNode(
          map = mapOf(
            "a" to PrimitiveNode(Value.NullValue, pos = LineColPos(line = 0, col = 3, source = "basic.yml")),
            "b" to MapNode(
              map = mapOf(
                "c" to PrimitiveNode(Value.StringNode(value = "hello"),
                  pos = LineColPos(line = 2, col = 5, source = "basic.yml")),
                "d" to PrimitiveNode(Value.NullValue, LineColPos(line = 3, col = 5, source = "basic.yml"))
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml")
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml")
        )
    }

    test("parsing nested basic arrays") {
      YamlParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.yml"), "basic.yml") shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(
              Value.StringNode(value = "hello"),
              pos = LineColPos(line = 0, col = 5, source = "basic.yml")
            ),
            "b" to ArrayNode(
              elements = listOf(
                PrimitiveNode(Value.StringNode(value = "x"), pos = LineColPos(line = 2, col = 4, source = "basic.yml")),
                PrimitiveNode(Value.StringNode(value = "y"), pos = LineColPos(line = 3, col = 4, source = "basic.yml")),
                PrimitiveNode(Value.StringNode(value = "z"), pos = LineColPos(line = 4, col = 4, source = "basic.yml"))
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml")
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml")
        )
    }

    test("parsing nested container arrays") {
      YamlParser().load(
        javaClass.getResourceAsStream("/nested_container_arrays.yml"),
        source = "basic.yml"
      ) shouldBe
        MapNode(
          mapOf(
            "a" to PrimitiveNode(
              Value.StringNode(value = "hello"),
              pos = LineColPos(line = 0, col = 5, source = "basic.yml")
            ),
            "b" to ArrayNode(
              elements = listOf(
                MapNode(
                  map = mapOf(
                    "c" to PrimitiveNode(Value.StringNode(value = "hello"),
                      pos = LineColPos(line = 2, col = 9, source = "basic.yml")),
                    "d" to PrimitiveNode(Value.StringNode(value = "true"),
                      pos = LineColPos(line = 3, col = 9, source = "basic.yml"))
                  ),
                  pos = LineColPos(line = 2, col = 4, source = "basic.yml")
                ),
                MapNode(
                  map = mapOf(
                    "e" to PrimitiveNode(Value.StringNode(value = "1.4"),
                      pos = LineColPos(line = 4, col = 9, source = "basic.yml")
                    ),
                    "f" to PrimitiveNode(Value.StringNode(value = "4"),
                      pos = LineColPos(line = 5, col = 9, source = "basic.yml")
                    )
                  ),
                  pos = LineColPos(line = 4, col = 4, source = "basic.yml")
                )
              ),
              pos = LineColPos(line = 2, col = 2, source = "basic.yml")
            )
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml")
        )
    }
  }
}
