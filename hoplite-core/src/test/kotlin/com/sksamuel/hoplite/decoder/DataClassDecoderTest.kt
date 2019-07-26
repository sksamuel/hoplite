package com.sksamuel.hoplite.decoder

import arrow.data.valid
import com.sksamuel.hoplite.BooleanValue
import com.sksamuel.hoplite.LongValue
import com.sksamuel.hoplite.MapValue
import com.sksamuel.hoplite.NullValue
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class DataClassDecoderTest : StringSpec() {
  init {
    "convert basic data class" {
      data class Foo(val a: String, val b: Long, val c: Boolean)
      DataClassDecoder(Foo::class).convert(
          MapValue(
              mapOf(
                  "a" to StringValue("hello", Pos.NoPos),
                  "b" to LongValue(123L, Pos.NoPos),
                  "c" to BooleanValue(true, Pos.NoPos)
              ),
              Pos.NoPos
          )
      ) shouldBe Foo("hello", 123, true).valid()
    }

    "support nulls" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)
      DataClassDecoder(Foo::class).convert(
          MapValue(
              mapOf(
                  "a" to NullValue(Pos.NoPos),
                  "b" to NullValue(Pos.NoPos),
                  "c" to NullValue(Pos.NoPos)
              ),
              Pos.NoPos
          )
      ) shouldBe Foo(null, null, null).valid()
    }

    "specified values should override null params" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)
      DataClassDecoder(Foo::class).convert(
          MapValue(
              mapOf(
                  "a" to StringValue("hello", Pos.NoPos),
                  "b" to LongValue(123L, Pos.NoPos),
                  "c" to BooleanValue(true, Pos.NoPos)
              ),
              Pos.NoPos
          )
      ) shouldBe Foo("hello", 123, true).valid()
    }
  }
}