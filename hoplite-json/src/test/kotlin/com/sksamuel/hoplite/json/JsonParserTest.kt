package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.StringValue
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import io.kotlintest.shouldBe

class JsonParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      JsonParser().load(javaClass.getResourceAsStream("/basic.json"), source = "a.json") shouldBe
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to LongValue(value = 1, pos = LineColPos(line = 3, col = 9, source = "a.json"), dotpath = "<root>.b"),
            "c" to BooleanValue(value = true, pos = LineColPos(line = 4, col = 12, source = "a.json"),
              dotpath = "<root>.c"),
            "d" to DoubleValue(value = 2.3, pos = LineColPos(line = 5, col = 11, source = "a.json"),
              dotpath = "<root>.d")
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json"),
          dotpath = "<root>"
        )
    }

    test("parsing basic json with comments") {
      JsonParser().load(javaClass.getResourceAsStream("/basic_with_comments.json"), source = "a.json") shouldBe
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello", pos = LineColPos(line = 3, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to LongValue(value = 1, pos = LineColPos(line = 5, col = 9, source = "a.json"), dotpath = "<root>.b"),
            "c" to BooleanValue(value = true, pos = LineColPos(line = 7, col = 12, source = "a.json"),
              dotpath = "<root>.c"),
            "d" to DoubleValue(value = 2.3, pos = LineColPos(line = 9, col = 11, source = "a.json"),
              dotpath = "<root>.d")
          ),
          pos = LineColPos(line = 1, col = 2, source = "a.json"),
          dotpath = "<root>"
        )
    }

    test("parsing nested basic arrays") {
      JsonParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.json"), source = "a.json") shouldBe
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello",
              pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to ListValue(
              elements = listOf(
                StringValue(value = "x", pos = LineColPos(line = 4, col = 8, source = "a.json"), dotpath = "<root>.b[0]"),
                StringValue(value = "y", pos = LineColPos(line = 5, col = 8, source = "a.json"), dotpath = "<root>.b[1]"),
                StringValue(value = "z", pos = LineColPos(line = 6, col = 8, source = "a.json"), dotpath = "<root>.b[2]")
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
        MapValue(
          mapOf(
            "a" to StringValue(value = "hello",
              pos = LineColPos(line = 2, col = 15, source = "a.json"),
              dotpath = "<root>.a"),
            "b" to ListValue(
              elements = listOf(
                MapValue(
                  map = mapOf(
                    "c" to StringValue(value = "hello", pos = LineColPos(line = 5, col = 19, source = "a.json"),
                      dotpath = "<root>.b[0].c"),
                    "d" to BooleanValue(value = true, pos = LineColPos(line = 6, col = 16, source = "a.json"),
                      dotpath = "<root>.b[0].d")
                  ),
                  pos = LineColPos(line = 4, col = 6, source = "a.json"),
                  dotpath = "<root>.b[0]"
                ),
                MapValue(
                  map = mapOf(
                    "e" to DoubleValue(value = 1.4, pos = LineColPos(line = 9, col = 15, source = "a.json"),
                      dotpath = "<root>.b[1].e"),
                    "f" to LongValue(value = 4, pos = LineColPos(line = 10, col = 13, source = "a.json"),
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
