package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
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

})
