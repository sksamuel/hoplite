package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.matchers.numerics.shouldBePositive
import io.kotlintest.matchers.string.shouldHaveLength
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class RandomPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String,
                  val c: Int,
                  val d: Boolean,
                  val e: String,
                  val f: String,
                  val g: String,
                  val h: Double,
                  val i: String
  )

  init {
    test("replace env vars") {
      for (k in 1..100) {
        val test = ConfigLoader().loadConfigOrThrow<Test>("/test_random_preprocessor.yml")
        test.a.length shouldBe 6
        test.b.length shouldBe 18
        test.c.shouldBePositive()
        test.e.length shouldBe 1
        test.f.length shouldBe 3
        test.i.shouldHaveLength(20)
      }

      (1..100).map {
        ConfigLoader().loadConfigOrThrow<Test>("/test_random_preprocessor.yml").d
      }.toSet() shouldBe setOf(true, false)
    }
  }
}
