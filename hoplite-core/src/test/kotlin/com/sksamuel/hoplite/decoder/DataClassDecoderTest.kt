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
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.reflect.full.createType

class DataClassDecoderTest : StringSpec() {
  init {
    "convert basic data class" {
      data class Foo(val a: String, val b: Long, val c: Boolean)

      val node = MapValue(
        mapOf(
          "a" to StringValue("hello", Pos.NoPos, dotpath = ""),
          "b" to LongValue(123L, Pos.NoPos, dotpath = ""),
          "c" to BooleanValue(true, Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo("hello", 123, true).valid()
    }

    "support nulls" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapValue(
        mapOf(
          "a" to NullValue(Pos.NoPos, dotpath = ""),
          "b" to NullValue(Pos.NoPos, dotpath = ""),
          "c" to NullValue(Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )

      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(null, null, null).valid()
    }

    "specified values should override null params" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapValue(
        mapOf(
          "a" to StringValue("hello", Pos.NoPos, dotpath = ""),
          "b" to LongValue(123L, Pos.NoPos, dotpath = ""),
          "c" to BooleanValue(true, Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
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

      val node = MapValue(
        mapOf(
          "a" to StringValue("1991", Pos.NoPos, dotpath = ""),
          "b" to LongValue(millis.toEpochMilli(), Pos.NoPos, dotpath = ""),
          "c" to StringValue("2007-12", Pos.NoPos, dotpath = ""),
          "d" to LongValue(millis.toEpochMilli(), Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(Year.of(1991), expectedDate, YearMonth.parse("2007-12"), expectedSqlTimestamp).valid()
    }

    "support ranges" {
      data class Foo(val a: IntRange, val b: LongRange, val c: CharRange)

      val node = MapValue(
        mapOf(
          "a" to StringValue("1..4", Pos.NoPos, dotpath = ""),
          "b" to StringValue("50..60", Pos.NoPos, dotpath = ""),
          "c" to StringValue("d..g", Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )
      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo(IntRange(1, 4), LongRange(50, 60), CharRange('d', 'g')).valid()
    }

    "support default values" {
      data class Foo(val a: String = "default a", val b: String = "default b", val c: Boolean = false)

      val node = MapValue(
        mapOf(
          "a" to StringValue("value", Pos.NoPos, dotpath = "")
        ),
        Pos.NoPos, dotpath = ""
      )

      DataClassDecoder().decode(node, Foo::class.createType(), defaultDecoderRegistry()) shouldBe
        Foo("value", "default b", false).valid()
    }
  }
}
