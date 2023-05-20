package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ArrayNode
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HoconParserTest : FunSpec() {
  init {

    test("parsing hocon") {
      HoconParser().load(javaClass.getResourceAsStream("/basic.conf"), "a.json") shouldBe
        MapNode(
          mapOf(
            "featureFlags" to MapNode(
              mapOf(
                "featureA" to StringNode("yes", Pos.LinePos(17, "a.json"), DotPath("featureFlags", "featureA")),
                "featureB" to BooleanNode(true, Pos.LinePos(18, "a.json"), DotPath("featureFlags", "featureB"))
              ),
              Pos.LinePos(16, "a.json"),
              DotPath("featureFlags")
            ),
            "toplevel" to StringNode("hello", Pos.LinePos(21, "a.json"), DotPath("toplevel")),
            "conf" to MapNode(
              mapOf(
                "missing" to NullNode(Pos.LinePos(13, "a.json"), DotPath("conf", "missing")),
                "complex" to MapNode(
                  mapOf(
                    "arrays" to ArrayNode(
                      listOf(
                        MapNode(
                          mapOf(
                            "b" to DoubleNode(
                              4.4,
                              Pos.LinePos(10, "a.json"),
                              DotPath("conf", "complex", "arrays", "b")
                            ),
                            "a" to StringNode(
                              "wibble",
                              Pos.LinePos(9, "a.json"),
                              DotPath("conf", "complex", "arrays", "a")
                            )
                          ),
                          Pos.LinePos(8, "a.json"),
                          DotPath("conf", "complex", "arrays")
                        )
                      ),
                      Pos.LinePos(7, "a.json"),
                      DotPath("conf", "complex", "arrays")
                    )
                  ),
                  Pos.LinePos(7, "a.json"),
                  DotPath("conf", "complex")
                ),
                "name" to StringNode("default", Pos.LinePos(2, "a.json"), DotPath("conf", "name")),
                "title" to StringNode("Simple Title", Pos.LinePos(3, "a.json"), DotPath("conf", "title")),
                "nested" to MapNode(
                  mapOf(
                    "whitelistIds" to ArrayNode(
                      listOf(
                        LongNode(1, Pos.LinePos(5, "a.json"), DotPath("conf", "nested", "whitelistIds")),
                        LongNode(22, Pos.LinePos(5, "a.json"), DotPath("conf", "nested", "whitelistIds")),
                        LongNode(34, Pos.LinePos(5, "a.json"), DotPath("conf", "nested", "whitelistIds"))
                      ),
                      Pos.LinePos(5, "a.json"), DotPath("conf", "nested", "whitelistIds")
                    )
                  ),
                  Pos.LinePos(4, "a.json"), DotPath("conf", "nested")
                )
              ),
              Pos.LinePos(1, "a.json"),
              DotPath("conf")
            )
          ),
          Pos.LinePos(1, "a.json"),
          DotPath.root
        )
    }
  }
}
