package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
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
                "featureA" to StringNode("yes", Pos.LinePos(17, "a.json"), "<root>.featureFlags.featureA"),
                "featureB" to BooleanNode(true, Pos.LinePos(18, "a.json"), "<root>.featureFlags.featureB")
              ),
              Pos.LinePos(16, "a.json"),
              "<root>.featureFlags"
            ),
            "toplevel" to StringNode("hello", Pos.LinePos(21, "a.json"), "<root>.toplevel"),
            "conf" to MapNode(
              mapOf(
                "missing" to NullNode(Pos.LinePos(13, "a.json"), "<root>.conf.missing"),
                "complex" to MapNode(
                  mapOf(
                    "arrays" to ListNode(
                      listOf(
                        MapNode(
                          mapOf(
                            "b" to DoubleNode(4.4, Pos.LinePos(10, "a.json"), "<root>.conf.complex.arrays[0].b"),
                            "a" to StringNode("wibble", Pos.LinePos(9, "a.json"), "<root>.conf.complex.arrays[0].a")
                          ),
                          Pos.LinePos(8, "a.json"),
                          "<root>.conf.complex.arrays[0]")
                      ),
                      Pos.LinePos(7, "a.json"),
                      "<root>.conf.complex.arrays")
                  ),
                  Pos.LinePos(7, "a.json"),
                  "<root>.conf.complex"
                ),
                "name" to StringNode("default", Pos.LinePos(2, "a.json"), "<root>.conf.name"),
                "title" to StringNode("Simple Title", Pos.LinePos(3, "a.json"), "<root>.conf.title"),
                "nested" to MapNode(
                  mapOf(
                    "whitelistIds" to ListNode(listOf(
                      LongNode(1, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[0]"),
                      LongNode(22, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[1]"),
                      LongNode(34, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[2]")
                    ),
                      Pos.LinePos(5, "a.json"),
                      "<root>.conf.nested.whitelistIds")
                  ),
                  Pos.LinePos(4, "a.json"),
                  "<root>.conf.nested"
                )
              ),
              Pos.LinePos(1, "a.json"),
              "<root>.conf")
          ),
          Pos.LinePos(1, "a.json"),
          "<root>"
        )
    }
  }
}
