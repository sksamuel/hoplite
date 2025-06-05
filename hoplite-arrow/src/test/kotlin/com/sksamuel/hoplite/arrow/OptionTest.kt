package com.sksamuel.hoplite.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class OptionTest : FunSpec({

  test("support Options") {
    data class Foo(val a: Option<String>,
                   val b: Option<Long>,
                   val c: Option<String>,
                   val d: Option<Long>,
                   val e: Option<UUID>)

    val foo = ConfigLoaderBuilder.default().build().loadConfigOrThrow<Foo>("/options.yml")
    foo.a shouldBe None
    foo.b shouldBe None
    foo.c shouldBe Some("hello")
    foo.d shouldBe Some(123L)
    foo.e shouldBe Some(UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d"))
  }
})
