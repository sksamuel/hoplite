package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some

class OptionDecoderTest : FunSpec({

  test("Options decoded from yaml") {
    data class Test(val a: Option<String>,
                    val b: Option<String>,
                    val c: Option<Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/options.yml")
    config.a shouldBe none()
    config.b shouldBe some("hello")
    config.c shouldBe some(123)
  }

  // OptionDecoder used to enumerate only the primitive node types (StringNode, LongNode, …),
  // so Option<DataClass> and Option<List<X>> failed with DecodeError despite the inner decoders
  // being perfectly capable.
  test("Option<DataClass> decodes a present nested data class") {
    data class Inner(val name: String, val count: Int)
    data class Outer(val inner: Option<Inner>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("inner.name" to "rimmer", "inner.count" to "3"))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.inner shouldBe some(Inner("rimmer", 3))
  }

  test("Option<List<String>> decodes a present list") {
    data class Outer(val xs: Option<List<String>>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("xs" to listOf("a", "b", "c")))
      .build()
      .loadConfigOrThrow<Outer>()

    cfg.xs shouldBe some(listOf("a", "b", "c"))
  }
})
