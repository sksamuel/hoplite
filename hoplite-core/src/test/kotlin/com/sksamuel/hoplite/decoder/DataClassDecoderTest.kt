package com.sksamuel.hoplite.decoder

import arrow.data.valid
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.PrimitiveNode
import com.sksamuel.hoplite.Value
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.reflect.full.createType

class DataClassDecoderTest : StringSpec() {
  init {
    "convert basic data class" {
      data class Foo(val a: String, val b: Long, val c: Boolean)

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.StringNode("hello"), Pos.NoPos),
          "b" to PrimitiveNode(Value.LongNode(123L), Pos.NoPos),
          "c" to PrimitiveNode(Value.BooleanNode(true), Pos.NoPos)
        ),
        Pos.NoPos
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo("hello", 123, true).valid()
    }

    "support nulls" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.NullValue, Pos.NoPos),
          "b" to PrimitiveNode(Value.NullValue, Pos.NoPos),
          "c" to PrimitiveNode(Value.NullValue, Pos.NoPos)
        ),
        Pos.NoPos
      )

      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(null, null, null).valid()
    }

    "specified values should override null params" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.StringNode("hello"), Pos.NoPos),
          "b" to PrimitiveNode(Value.LongNode(123L), Pos.NoPos),
          "c" to PrimitiveNode(Value.BooleanNode(true), Pos.NoPos)
        ),
        Pos.NoPos
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo("hello", 123, true).valid()
    }

    "support Date types" {
      data class Foo(val a: Year, val b: java.util.Date, val c: YearMonth, val d: java.sql.Timestamp)

      val millis = LocalDate.parse("2011-08-17")
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
      val expectedDate = java.util.Date.from(millis)
      val expectedSqlTimestamp = java.sql.Timestamp(millis.toEpochMilli())

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.StringNode("1991"), Pos.NoPos),
          "b" to PrimitiveNode(Value.LongNode(millis.toEpochMilli()), Pos.NoPos),
          "c" to PrimitiveNode(Value.StringNode("2007-12"), Pos.NoPos),
          "d" to PrimitiveNode(Value.LongNode(millis.toEpochMilli()), Pos.NoPos)
        ),
        Pos.NoPos
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(Year.of(1991), expectedDate, YearMonth.parse("2007-12"), expectedSqlTimestamp).valid()
    }

    "support ranges" {
      data class Foo(val a: IntRange, val b: LongRange, val c: CharRange)

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.StringNode("1..4"), Pos.NoPos),
          "b" to PrimitiveNode(Value.StringNode("50..60"), Pos.NoPos),
          "c" to PrimitiveNode(Value.StringNode("d..g"), Pos.NoPos)
        ),
        Pos.NoPos
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(IntRange(1, 4), LongRange(50, 60), CharRange('d', 'g')).valid()
    }

    "support default values" {
      data class Foo(val a: String = "default a", val b: String = "default b", val c: Boolean = false)

      val node = MapNode(
        mapOf(
          "a" to PrimitiveNode(Value.StringNode("value"), Pos.NoPos)
        ),
        Pos.NoPos
      )

      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo("value", "default b", false).valid()
    }
  }
}
