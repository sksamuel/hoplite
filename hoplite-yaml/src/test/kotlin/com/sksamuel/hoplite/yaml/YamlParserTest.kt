package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.StringNode
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos
import io.kotlintest.shouldBe

class YamlParserTest : FunSpec() {
  init {

    test("parsing basic json") {
      YamlParser().load(javaClass.getResourceAsStream("/basic.yml")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to StringNode(value = "1", pos = LineColPos(line = 1, col = 5)),
                  "c" to StringNode(value = "true", pos = LineColPos(line = 2, col = 5)),
                  "d" to StringNode(value = "2.3", pos = LineColPos(line = 3, col = 5))
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }

    test("parsing null fields") {
      YamlParser().load(javaClass.getResourceAsStream("/nulls.yml")) shouldBe
          MapNode(
              map = mapOf(
                  "a" to NullNode(pos = LineColPos(line = 0, col = 3)),
                  "b" to MapNode(
                      map = mapOf(
                          "c" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 5)),
                          "d" to NullNode(pos = LineColPos(line = 3, col = 5))
                      ),
                      pos = LineColPos(line = 2, col = 2)
                  )
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }

    test("parsing nested basic arrays") {
      YamlParser().load(javaClass.getResourceAsStream("/nested_basic_arrays.yml")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to ListNode(
                      elements = listOf(
                          StringNode(value = "x", pos = LineColPos(line = 2, col = 4)),
                          StringNode(value = "y", pos = LineColPos(line = 3, col = 4)),
                          StringNode(value = "z", pos = LineColPos(line = 4, col = 4))
                      ),
                      pos = LineColPos(line = 2, col = 2)
                  )
              ),
              pos = LineColPos(line = 0, col = 0)
          )
    }

    test("parsing nested container arrays") {
      YamlParser().load(javaClass.getResourceAsStream("/nested_container_arrays.yml")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 0, col = 5)),
                  "b" to ListNode(
                      elements = listOf(
                          MapNode(
                              map = mapOf(
                                  "c" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 9)),
                                  "d" to StringNode(value = "true", pos = LineColPos(line = 3, col = 9))
                              ),
                              pos = LineColPos(line = 2, col = 4)
                          ),
                          MapNode(
                              map = mapOf(
                                  "e" to StringNode(value = "1.4", pos = LineColPos(line = 4, col = 9)),
                                  "f" to StringNode(value = "4", pos = LineColPos(line = 5, col = 9))
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