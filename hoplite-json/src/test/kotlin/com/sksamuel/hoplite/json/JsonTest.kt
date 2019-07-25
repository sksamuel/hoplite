package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos

class JsonTest : FunSpec() {
  init {

    test("parsing basic json") {
      Json.load(javaClass.getResourceAsStream("/basic.json")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to LongValue(value = 1, pos = LineColPos(line = 3, col = 9)),
                  "c" to BooleanValue(value = true, pos = LineColPos(line = 4, col = 12)),
                  "d" to DoubleValue(value = 2.3, pos = LineColPos(line = 5, col = 11))
              ),
              pos = LineColPos(line = 1, col = 2)
          )
    }

    test("parsing nested basic arrays") {
      Json.load(javaClass.getResourceAsStream("/nested_basic_arrays.json")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to ListValue(
                      values = listOf(
                          StringValue(value = "x", pos = LineColPos(line = 4, col = 8)),
                          StringValue(value = "y", pos = LineColPos(line = 5, col = 8)),
                          StringValue(value = "z", pos = LineColPos(line = 6, col = 8))
                      ),
                      pos = LineColPos(line = 3, col = 9)
                  )
              ),
              pos = LineColPos(line = 1, col = 2)
          )
    }

    test("parsing nested container arrays") {
      Json.load(javaClass.getResourceAsStream("/nested_container_arrays.json")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to ListValue(
                      values = listOf(
                          MapValue(
                              map = mapOf(
                                  "c" to StringValue(value = "hello", pos = LineColPos(line = 5, col = 19)),
                                  "d" to BooleanValue(value = true, pos = LineColPos(line = 6, col = 16))
                              ),
                              pos = LineColPos(line = 4, col = 6)
                          ),
                          MapValue(
                              map = mapOf(
                                  "e" to DoubleValue(value = 1.4, pos = LineColPos(line = 9, col = 15)),
                                  "f" to LongValue(value = 4, pos = LineColPos(line = 10, col = 13))
                              ),
                              pos = LineColPos(line = 8, col = 6)
                          )
                      ),
                      pos = LineColPos(line = 3, col = 9)
                  )
              ),
              pos = LineColPos(line = 1, col = 2)
          )
    }
  }
}