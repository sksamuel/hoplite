package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.security.Principal

class PrincipalDecoderTest : StringSpec({
  "Principal decoded from yaml" {
    data class Test(val name: Principal)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/principal.yaml")
    config shouldBe Test(BasicPrincipal("sammy"))
  }
})
