package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class HoconParserTest : FunSpec() {
  init {

    test("parsing hocon") {
      HoconParser().load(javaClass.getResourceAsStream("/basic.conf"), "a.json") shouldBe
        MapValue(
          mapOf(
            "featureFlags" to MapValue(
              mapOf(
                "featureA" to StringValue("yes", Pos.LinePos(17, "a.json"), "<root>.featureFlags.featureA"),
                "featureB" to BooleanValue(true, Pos.LinePos(18, "a.json"), "<root>.featureFlags.featureB")
              ),
              Pos.LinePos(16, "a.json"),
              "<root>.featureFlags"
            ),
            "toplevel" to StringValue("hello", Pos.LinePos(21, "a.json"), "<root>.toplevel"),
            "conf" to MapValue(
              mapOf(
                "missing" to NullValue(Pos.LinePos(13, "a.json"), "<root>.conf.missing"),
                "complex" to MapValue(
                  mapOf(
                    "arrays" to ListValue(
                      listOf(
                        MapValue(
                          mapOf(
                            "b" to DoubleValue(4.4, Pos.LinePos(10, "a.json"), "<root>.conf.complex.arrays[0].b"),
                            "a" to StringValue("wibble", Pos.LinePos(9, "a.json"), "<root>.conf.complex.arrays[0].a")
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
                "name" to StringValue("default", Pos.LinePos(2, "a.json"), "<root>.conf.name"),
                "title" to StringValue("Simple Title", Pos.LinePos(3, "a.json"), "<root>.conf.title"),
                "nested" to MapValue(
                  mapOf(
                    "whitelistIds" to ListValue(listOf(
                      LongValue(1, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[0]"),
                      LongValue(22, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[1]"),
                      LongValue(34, Pos.LinePos(5, "a.json"), "<root>.conf.nested.whitelistIds[2]")
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
