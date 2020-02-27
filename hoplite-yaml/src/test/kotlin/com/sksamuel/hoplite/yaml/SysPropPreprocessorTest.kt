package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.FunSpec

class SysPropPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String,
                  val c: String,
                  val d: String,
                  val e: String,
                  val f: String,
                  val g: String,
                  val h: String)

  init {
    test("replace placeholders with system properties") {
      withSystemProperty("wibble", "wobble") {
        ConfigLoader().loadConfigOrThrow<Test>("/test_sysproperty_replacement.yml") shouldBe
          Test(a = "foo",
            b = "wobble",
            c = "aawobble",
            d = "wobblebb",
            e = "aawobblebb",
            f = "\${unknown}",
            g = "\$wibble",
            h = "\${unknown}\$wibble"
          )
      }
    }
  }
}
