package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBePositive
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength

class RandomPreprocessorTest : FunSpec() {

  data class Test(
                  val c: Int,
                  val d: Boolean,
                  val e: String,
                  val f: String,
                  val g: String,
                  val h: Double,
                  val i: String,
                  val uuid: String
  )

  init {
    test("replace env vars") {
      for (k in 1..100) {
        val test = ConfigLoader().loadConfigOrThrow<Test>("/test_random_preprocessor.yml")
        test.c.shouldBePositive()
        test.e.length shouldBe 1
        test.f.length shouldBe 3
        test.i.shouldHaveLength(20)
        test.uuid.shouldHaveLength(36)
      }

      (1..100).map {
        ConfigLoader().loadConfigOrThrow<Test>("/test_random_preprocessor.yml").d
      }.toSet() shouldBe setOf(true, false)
    }
  }
}
