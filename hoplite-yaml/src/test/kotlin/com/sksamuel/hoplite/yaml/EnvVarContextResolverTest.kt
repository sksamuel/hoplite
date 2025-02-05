package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalHoplite::class)
class EnvVarContextResolverTest : FunSpec() {
  data class Config(
    val foo: String,
    val bar: String,
    val baz: String,
    val blopp: String
  )

  init {
    test("replace env vars") {
      withEnvironment(mapOf("FOO" to "bar", "BAR" to "bar")) {
        val config = ConfigLoaderBuilder.newBuilder()
          .build()
          .loadConfigOrThrow<Config>("/test_env_replacement3.yml")

        config shouldBe Config(
          foo = "bar",
          bar = "baz",
          baz = "foobarbaz",
          blopp = "foobarblopp"
        )
      }
    }

    test("env replacement with unknown value should error") {
      data class Test(val a: String)
      shouldThrowAny {
        ConfigLoaderBuilder.newBuilder()
          .addSource(YamlPropertySource("a: \${{ env:foobarbaz }}"))
          .build()
          .loadConfigOrThrow<Test>()
      }
        .message.shouldContain("'a': Could not resolve 'foobarbaz'")
    }
  }
}
