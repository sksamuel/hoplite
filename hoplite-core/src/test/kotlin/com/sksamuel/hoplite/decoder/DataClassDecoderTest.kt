package com.sksamuel.hoplite.decoder

import arrow.data.valid
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.reflect.full.createType

class DataClassDecoderTest : StringSpec() {
  init {
    "convert basic data class" {
      data class Foo(val a: String, val b: Long, val c: Boolean)

      val node = MapNode(
        mapOf(
          "a" to StringNode("hello", Pos.NoPos, dotpath = ""),
          "b" to LongNode(123L, Pos.NoPos, dotpath = ""),
          "c" to BooleanNode(true, Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry(), "") shouldBe
        Foo("hello", 123, true).valid()
    }

    "support nulls" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to NullNode(Pos.NoPos, dotpath = ""),
          "b" to NullNode(Pos.NoPos, dotpath = ""),
          "c" to NullNode(Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )

      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry(), "") shouldBe
        Foo(null, null, null).valid()
    }

    "specified values should override null params" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to StringNode("hello", Pos.NoPos, dotpath = ""),
          "b" to LongNode(123L, Pos.NoPos, dotpath = ""),
          "c" to BooleanNode(true, Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry(), "") shouldBe
        Foo("hello", 123, true).valid()
    }
  }
}
