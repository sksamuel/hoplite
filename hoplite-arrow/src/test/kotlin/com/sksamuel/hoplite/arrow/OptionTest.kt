package com.sksamuel.hoplite.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.*

class OptionTest : FunSpec({

  test("support Options") {
    data class Foo(val a: Option<String>,
                   val b: Option<Long>,
                   val c: Option<String>,
                   val d: Option<Long>,
                   val e: Option<UUID>)

    val foo = ConfigLoader().loadConfigOrThrow<Foo>("/options.yml")
    foo.a shouldBe None
    foo.b shouldBe None
    foo.c shouldBe Some("hello")
    foo.d shouldBe Some(123L)
    foo.e shouldBe Some(UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d"))
  }

  // OptionDecoder used to enumerate only the primitive node types (StringNode, LongNode, …),
  // so Option<DataClass> and Option<List<X>> failed with DecodeError despite the inner decoders
  // being perfectly capable. Defer to the inner decoder for non-null/undefined nodes.
  test("Option<DataClass> decodes a present nested data class") {
    data class Inner(val name: String, val count: Int)
    data class Outer(val inner: Option<Inner>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("inner.name" to "rimmer", "inner.count" to "3"))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.inner shouldBe Some(Inner("rimmer", 3))
  }

  test("Option<List<String>> decodes a present list") {
    data class Outer(val xs: Option<List<String>>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("xs" to listOf("a", "b", "c")))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.xs shouldBe Some(listOf("a", "b", "c"))
  }
})
