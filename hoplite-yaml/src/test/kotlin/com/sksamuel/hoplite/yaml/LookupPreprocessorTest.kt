package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LookupPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String,
                  val c: String,
                  val d: String,
                  val e: String
  )

  init {
    test("lookup preprocessor") {
      ConfigLoader().loadConfigOrThrow<Test>("/test_lookup.yml") shouldBe
        Test(
          a = "foo",
          b = "bfoo",
          c = "cfoo.hello",
          d = "ddef.boo",
          e = "e\${qwe}"
        )
    }
  }
}
