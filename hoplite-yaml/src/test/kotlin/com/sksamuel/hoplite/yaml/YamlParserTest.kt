package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.DoubleValue
import com.sksamuel.hoplite.ListValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.StringValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import com.sksamuel.hoplite.yaml.com.sksamuel.hoplite.yaml.YamlParser

class YamlParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      YamlParser.load(javaClass.getResourceAsStream("/basic.yml")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to StringValue(value = "1", pos = LineColPos(line = 1, col = 5)),
                  "c" to StringValue(value = "true", pos = LineColPos(line = 2, col = 5)),
                  "d" to StringValue(value = "2.3", pos = LineColPos(line = 3, col = 5))
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }

    test("parsing nested basic arrays") {
      YamlParser.load(javaClass.getResourceAsStream("/nested_basic_arrays.yml")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to ListValue(
                      values = listOf(
                          StringValue(value = "x", pos = LineColPos(line = 2, col = 4)),
                          StringValue(value = "y", pos = LineColPos(line = 3, col = 4)),
                          StringValue(value = "z", pos = LineColPos(line = 4, col = 4))
                      ),
                      pos = LineColPos(line = 2, col = 2)
                  )
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }

    test("parsing nested container arrays") {
      YamlParser.load(javaClass.getResourceAsStream("/nested_container_arrays.yml")) shouldBe
          MapValue(
              mapOf(
                  "a" to StringValue(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to ListValue(
                      values = listOf(
                          MapValue(
                              value = mapOf(
                                  "c" to StringValue(value = "hello", pos = LineColPos(line = 2, col = 9)),
                                  "d" to StringValue(value = "true", pos = LineColPos(line = 3, col = 9))
                              ),
                              pos = LineColPos(line = 2, col = 4)
                          ),
                          MapValue(
                              value = mapOf(
                                  "e" to StringValue(value = "1.4", pos = LineColPos(line = 4, col = 9)),
                                  "f" to StringValue(value = "4", pos = LineColPos(line = 5, col = 9))
                              ),
                              pos = LineColPos(line = 4, col = 4)
                          )
                      ),
                      pos = LineColPos(line = 2, col = 2)
                  )
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }
  }
}