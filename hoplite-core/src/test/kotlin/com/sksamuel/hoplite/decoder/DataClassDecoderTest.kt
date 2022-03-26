package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.defaultParamMappers
import com.sksamuel.hoplite.fp.valid
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.reflect.full.createType

enum class FooEnum { FIRST, SECOND, THIRD }
class DataClassDecoderTest : StringSpec() {
  init {
    "convert basic data class" {
      data class Foo(val a: String, val b: Long, val c: Boolean)

      val node = MapNode(
        mapOf(
          "a" to StringNode("hello", Pos.NoPos, DotPath.root),
          "b" to LongNode(123L, Pos.NoPos, DotPath.root),
          "c" to BooleanNode(true, Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root
      )
      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo("hello", 123, true).valid()
    }

    "support nulls" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to NullNode(Pos.NoPos, DotPath.root),
          "b" to NullNode(Pos.NoPos, DotPath.root),
          "c" to NullNode(Pos.NoPos, DotPath.root)
        ), Pos.NoPos,
        DotPath.root
      )

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(null, null, null).valid()
    }

    "specified values should override null params" {
      data class Foo(val a: String?, val b: Long?, val c: Boolean?)

      val node = MapNode(
        mapOf(
          "a" to StringNode("hello", Pos.NoPos, DotPath.root),
          "b" to LongNode(123L, Pos.NoPos, DotPath.root),
          "c" to BooleanNode(true, Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root,
      )
      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo("hello", 123, true).valid()
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
          "a" to StringNode("1991", Pos.NoPos, DotPath.root),
          "b" to LongNode(millis.toEpochMilli(), Pos.NoPos, DotPath.root),
          "c" to StringNode("2007-12", Pos.NoPos, DotPath.root),
          "d" to LongNode(millis.toEpochMilli(), Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root,
      )
      DataClassDecoder().decode(node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(Year.of(1991), expectedDate, YearMonth.parse("2007-12"), expectedSqlTimestamp).valid()
    }

    "support ranges" {
      data class Foo(val a: IntRange, val b: LongRange, val c: CharRange)

      val node = MapNode(
        mapOf(
          "a" to StringNode("1..4", Pos.NoPos, DotPath.root),
          "b" to StringNode("50..60", Pos.NoPos, DotPath.root),
          "c" to StringNode("d..g", Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root
      )
      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(IntRange(1, 4), LongRange(50, 60), CharRange('d', 'g')).valid()
    }

    "support default values" {
      data class Foo(val a: String = "default a", val b: String = "default b", val c: Boolean = false)

      val node = MapNode(
        mapOf(
          "a" to StringNode("value", Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root,
      )

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo("value", "default b", false).valid()
    }

    "supports single param value constructor" {
      data class Foo(val value: FooEnum)

      val node = StringNode("SECOND", Pos.NoPos, DotPath.root)

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(FooEnum.SECOND).valid()
    }

    "supports multiple constructors with single param value constructor" {
      data class Foo(val a: FooEnum, val b: String?, val c: Boolean?){
        constructor(value: FooEnum) : this( value, null, null)
      }

      val node = StringNode("THIRD", Pos.NoPos, DotPath.root)

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(FooEnum.THIRD).valid()
    }

    "calls multiple param constructors with type that has single param value constructor" {
      data class Foo(val a: FooEnum, val b: String?, val c: Boolean?){
        constructor(value: FooEnum) : this( value, null, null)
      }

      val node = MapNode(
        mapOf(
          "a" to StringNode("FIRST", Pos.NoPos, DotPath.root),
          "b" to StringNode("MultiParamCallExpected", Pos.NoPos, DotPath.root),
          "c" to BooleanNode(false, Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root
      )

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(FooEnum.FIRST, "MultiParamCallExpected", false).valid()
    }

    "calls partial param constructors with type that has single param value constructor" {
      data class Foo(val a: FooEnum, val b: String?, val c: Boolean?){
        constructor(value: FooEnum) : this( value, null, null)
        constructor(value: FooEnum, c: Boolean) : this( value, null, c)
      }

      val node = MapNode(
        mapOf(
          "a" to StringNode("THIRD", Pos.NoPos, DotPath.root),
          "c" to BooleanNode(true, Pos.NoPos, DotPath.root)
        ),
        Pos.NoPos,
        DotPath.root
      )

      DataClassDecoder().decode(
        node,
        Foo::class.createType(),
        DecoderContext(defaultDecoderRegistry(), defaultParamMappers())
      ) shouldBe Foo(FooEnum.THIRD, true).valid()
    }
  }
}
