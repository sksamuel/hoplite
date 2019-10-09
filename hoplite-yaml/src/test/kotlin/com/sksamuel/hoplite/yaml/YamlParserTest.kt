package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.StringValue
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import io.kotlintest.shouldBe

class YamlParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      YamlParser().load(javaClass.getResourceAsStream("/basic.yml"), "basic.yml") shouldBe
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to StringValue(value = "1",
              pos = LineColPos(line = 1, col = 5, source = "basic.yml"),
              dotpath = "<root>.b"),
            "c" to StringValue(value = "true",
              pos = LineColPos(line = 2, col = 5, source = "basic.yml"),
              dotpath = "<root>.c"),
            "d" to StringValue(value = "2.3",
              pos = LineColPos(line = 3, col = 5, source = "basic.yml"),
              dotpath = "<root>.d")
          ),
          pos = LineColPos(line = 0, col = 0, source = "basic.yml"),
          dotpath = "<root>"
        )
    }

    test("parsing null fields") {
      YamlParser().load(javaClass.getResourceAsStream("/nulls.yml"), source = "basic.yml") shouldBe
        MapValue(
          map = mapOf(
            "a" to NullValue(pos = LineColPos(line = 0, col = 3, source = "basic.yml"), dotpath = "<root>.a"),
            "b" to MapValue(
              map = mapOf(
                "c" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 5, source = "basic.yml"),
                  dotpath = "<root>.b.c"),
                "d" to NullValue(pos = LineColPos(line = 3, col = 5, source = "basic.yml"), dotpath = "<root>.b.d")
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
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to ListValue(
              elements = listOf(
                StringValue(value = "x", pos = LineColPos(line = 2, col = 4, source = "basic.yml"), dotpath = "<root>.b[0]"),
                StringValue(value = "y", pos = LineColPos(line = 3, col = 4, source = "basic.yml"), dotpath = "<root>.b[1]"),
                StringValue(value = "z", pos = LineColPos(line = 4, col = 4, source = "basic.yml"), dotpath = "<root>.b[2]")
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
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello",
              pos = LineColPos(line = 0, col = 5, source = "basic.yml"),
              dotpath = "<root>.a"),
            "b" to ListValue(
              elements = listOf(
                MapValue(
                  map = mapOf(
                    "c" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[0].c"),
                    "d" to StringValue(value = "true", pos = LineColPos(line = 3, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[0].d")
                  ),
                  pos = LineColPos(line = 2, col = 4, source = "basic.yml"),
                  dotpath = "<root>.b[0]"
                ),
                MapValue(
                  map = mapOf(
                    "e" to StringValue(value = "1.4",
                      pos = LineColPos(line = 4, col = 9, source = "basic.yml"),
                      dotpath = "<root>.b[1].e"),
                    "f" to StringValue(value = "4",
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
