package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.decoder.BasicPrincipal
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.security.Principal

class PrincipalDecoderTest : StringSpec({
  "Principal decoded from yaml" {
    data class Test(val name: Principal)
    ConfigLoader().loadConfig<Test>("/principal.yaml").shouldBeValid {
      it.a shouldBe Test(BasicPrincipal("sammy"))
    }
  }
})