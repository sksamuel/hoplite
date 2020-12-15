package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.Set
import io.vavr.kotlin.linkedHashSet

class SetDecoderTest : FunSpec({

  test("Set<Int> decoded from yaml") {
    data class Test(val numbers: Set<Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_set.yml")
    config shouldBe Test(linkedHashSet(1, 2, 3))
  }

})
