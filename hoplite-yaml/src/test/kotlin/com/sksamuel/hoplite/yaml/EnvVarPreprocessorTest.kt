package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class EnvVarPreprocessorTest : FunSpec() {

  data class Test(val a: String,
                  val b: String,
                  val c: String,
                  val d: String,
                  val e: String,
                  val f: String,
                  val g: String,
                  val h: String,
  )

  data class Test2(
    val a: String,
    val b: Map<String, String>
  )

  init {
    test("replace env vars") {
      withEnvironment("wibble", "wobble") {
        ConfigLoader().loadConfigOrThrow<Test>("/test_env_replacement.yml") shouldBe
          Test(
            a = "foo",
            b = "wobble",
            c = "aawobble",
            d = "wobblebb",
            e = "aawobblebb",
            f = "\$wibble",
            g = "default",
            h = "wobble"
          )
      }
    }

    test("env var replacement should work in maps") {
      withEnvironment(mapOf("AA" to "foo", "CC" to "bar")) {
        ConfigLoader().loadConfigOrThrow<Test2>("/test_env_replacement2.yml") shouldBe
          Test2(a = "foo", b = mapOf("c" to "bar"))
      }
    }

    test("env replacement with unknown value should error") {
      data class Test(val a: String)
      shouldThrowAny {
        ConfigLoader.builder()
          .addSource(YamlPropertySource("a: \${wibble}"))
          .build()
          .loadConfigOrThrow<Test>()
      }.message shouldBe "Unknown replacement value: wibble"
    }
  }
}
