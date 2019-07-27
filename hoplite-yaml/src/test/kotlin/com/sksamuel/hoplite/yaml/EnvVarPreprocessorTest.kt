package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec

class EnvVarPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String,
                  val c: String,
                  val d: String,
                  val e: String,
                  val f: String)

  init {
    test("replace env vars") {
      withEnvironment("wibble", "wobble") {
        ConfigLoader().loadConfigOrThrow<Test>("/test_env_replacement.yml") shouldBe
            Test(a = "foo", b = "wibble", c = "aawibble", d = "wibblebb", e = "aawibblebb", f = "unknown")
      }
    }
  }
}