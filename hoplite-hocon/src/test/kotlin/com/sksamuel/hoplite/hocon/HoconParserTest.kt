package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class HoconParserTest : FunSpec() {
  init {

    test("parsing hocon") {
      HoconParser().load(javaClass.getResourceAsStream("/basic.conf"), "a.json") shouldBe
        MapNode(
          mapOf(
            "featureFlags" to MapNode(
              mapOf(
                "featureA" to PrimitiveNode(Value.StringNode("yes"), Pos.LinePos(17, "a.json")),
                "featureB" to PrimitiveNode(Value.BooleanNode(true), Pos.LinePos(18, "a.json"))
              ),
              Pos.LinePos(16, "a.json")
            ),
            "toplevel" to PrimitiveNode(Value.StringNode("hello"), Pos.LinePos(21, "a.json")),
            "conf" to MapNode(
              mapOf(
                "missing" to PrimitiveNode(Value.NullValue, Pos.LinePos(13, "a.json")),
                "complex" to MapNode(
                  mapOf(
                    "arrays" to ArrayNode(
                      listOf(
                        MapNode(
                          mapOf(
                            "b" to PrimitiveNode(Value.DoubleNode(4.4), Pos.LinePos(10, "a.json")),
                              "a" to PrimitiveNode(Value.StringNode("wibble"), Pos.LinePos(9, "a.json"))
                          ),
                          Pos.LinePos(8, "a.json")
                        )
                      ),
                      Pos.LinePos(7, "a.json")
                    )
                  ),
                  Pos.LinePos(7, "a.json")
                ),
                "name" to PrimitiveNode(Value.StringNode("default"), Pos.LinePos(2, "a.json")),
                "title" to PrimitiveNode(Value.StringNode("Simple Title"), Pos.LinePos(3, "a.json")),
                "nested" to MapNode(
                  mapOf(
                    "whitelistIds" to ArrayNode(
                      listOf(
                        PrimitiveNode(Value.LongNode(1), Pos.LinePos(5, "a.json")),
                        PrimitiveNode(Value.LongNode(22), Pos.LinePos(5, "a.json")),
                        PrimitiveNode(Value.LongNode(34), Pos.LinePos(5, "a.json")
                        )
                    ),
                      Pos.LinePos(5, "a.json")
                    )
                  ),
                  Pos.LinePos(4, "a.json")
                )
              ),
              Pos.LinePos(1, "a.json")
            )
          ),
          Pos.LinePos(1, "a.json")
        )
    }
  }
}
