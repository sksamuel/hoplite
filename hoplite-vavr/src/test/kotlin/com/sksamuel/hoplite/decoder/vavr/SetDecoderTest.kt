package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.List
import io.vavr.kotlin.list

class SetDecoderTest : FunSpec({

  test("Set<Int> decoded from yaml") {
    data class Test(val numbers: List<Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_set.yml")
    config shouldBe Test(list(1, 2, 3))
  }

})
