package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LookupPreprocessorTest : FunSpec() {

  data class Test(
    val a: String,
    val b: String,
    val c: String,
    val d: String,
    val e: String,
    val f: String
  )

  data class Test2(
    val env: String,
    val hostname: String
  )

  init {

    test("lookup preprocessor") {
      ConfigLoader().loadConfigOrThrow<Test>("/test_lookup.yml") shouldBe
        Test(
          a = "foo",
          b = "bfoo",
          c = "cfoo.hello",
          d = "ddef.boo",
          e = "E",
          f = "f\${qwe}"
        )
    }

    test("lookup preprocessor with react syntax") {
      ConfigLoader().loadConfigOrThrow<Test>("/test_lookup_react.yml") shouldBe
        Test(
          a = "foo",
          b = "bfoo",
          c = "cfoo.hello",
          d = "dE.boo",
          e = "E",
          f = "f{{unknown}}"
        )
    }

    test("lookup preprocessor across multiple files") {
      ConfigLoader().loadConfigOrThrow<Test2>("/test_lookup1.yml", "/test_lookup2.yml") shouldBe
        Test2(
          env = "PROD",
          hostname = "wibble.PROD"
        )
    }
  }
}
