package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe

class SysPropPreprocessorTest : FunSpec() {

  data class Test(
    val a: String,
    val b: String,
    val c: String,
    val d: String,
    val e: String,
    val f: String
  )

  init {
    test("replace placeholders with system properties") {
      withSystemProperty("wibble", "wobble") {
        ConfigLoader().loadConfigOrThrow<Test>("/test_sysproperty_replacement.yml") shouldBe
          Test(
            a = "foo",
            b = "wobble",
            c = "aawobble",
            d = "wobblebb",
            e = "aawobblebb",
            f = "\$wibble"
          )
      }
    }
  }
}
