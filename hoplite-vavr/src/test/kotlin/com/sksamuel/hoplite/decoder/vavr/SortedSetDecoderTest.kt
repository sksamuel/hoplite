package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.SortedSet
import io.vavr.kotlin.treeSet

class SortedSetDecoderTest : FunSpec({

  test("SortedSet<Int> decoded from yaml") {
    data class Test(val numbers: SortedSet<Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_set.yml")
    config shouldBe Test(treeSet(3, 2, 1))
  }

})
