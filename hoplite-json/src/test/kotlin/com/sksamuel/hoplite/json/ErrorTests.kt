package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

data class Foo(val wrongType: Boolean, val missing: String, val notalist: List<String>)

class ErrorTests : StringSpec({

  "!error handling" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/error1.json")
    }.message shouldBe ""
  }

})
