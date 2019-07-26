package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.StringNode
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import com.sksamuel.hoplite.Pos.LineColPos

class JsonTest : FunSpec() {
  init {

    test("parsing basic json") {
      Json.load(javaClass.getResourceAsStream("/basic.json")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to LongNode(value = 1, pos = LineColPos(line = 3, col = 9)),
                  "c" to BooleanNode(value = true, pos = LineColPos(line = 4, col = 12)),
                  "d" to DoubleNode(value = 2.3, pos = LineColPos(line = 5, col = 11))
              ),
              pos = LineColPos(line = 1, col = 2)
          )
    }

    test("parsing nested basic arrays") {
      Json.load(javaClass.getResourceAsStream("/nested_basic_arrays.json")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to ListNode(
                      elements = listOf(
                          StringNode(value = "x", pos = LineColPos(line = 4, col = 8)),
                          StringNode(value = "y", pos = LineColPos(line = 5, col = 8)),
                          StringNode(value = "z", pos = LineColPos(line = 6, col = 8))
                      ),
                      pos = LineColPos(line = 3, col = 9)
                  )
              ),
              pos = LineColPos(line = 1, col = 2)
          )
    }

    test("parsing nested container arrays") {
      Json.load(javaClass.getResourceAsStream("/nested_container_arrays.json")) shouldBe
          MapNode(
              mapOf(
                  "a" to StringNode(value = "hello", pos = LineColPos(line = 2, col = 15)),
                  "b" to ListNode(
                      elements = listOf(
                          MapNode(
                              map = mapOf(
                                  "c" to StringNode(value = "hello", pos = LineColPos(line = 5, col = 19)),
                                  "d" to BooleanNode(value = true, pos = LineColPos(line = 6, col = 16))
                              ),
                              pos = LineColPos(line = 4, col = 6)
                          ),
                          MapNode(
                              map = mapOf(
                                  "e" to DoubleNode(value = 1.4, pos = LineColPos(line = 9, col = 15)),
                                  "f" to LongNode(value = 4, pos = LineColPos(line = 10, col = 13))
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