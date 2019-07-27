package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.yaml.com.sksamuel.hoplite.yaml.Yaml
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SetDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val a: Set<Long>, val b: Set<String>)
    ConfigLoader(Yaml).loadConfig<Test>("/sets.yml").shouldBeValid {
      it.a shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    }
  }
})