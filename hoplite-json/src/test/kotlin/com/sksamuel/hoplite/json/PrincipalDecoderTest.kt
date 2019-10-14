package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.decoder.BasicPrincipal
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.security.Principal

class PrincipalDecoderTest : StringSpec({
  "Principal decoded from json" {
    data class Test(val name: Principal)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/principal.json")
    config shouldBe Test(BasicPrincipal("sammy"))
  }
})
