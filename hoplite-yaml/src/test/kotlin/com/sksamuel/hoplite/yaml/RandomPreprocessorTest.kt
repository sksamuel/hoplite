package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class RandomPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String)

  init {
    test("replace env vars") {
      val test = ConfigLoader().loadConfigOrThrow<Test>("/test_random_preprocessor.yml")
      test.a.length shouldBe 6
      test.b.length shouldBe 18
    }
  }
}
