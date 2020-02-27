package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.decoder.BasicPrincipal
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.security.Principal

class PrincipalDecoderTest : StringSpec({
  "Principal decoded from json" {
    data class Test(val name: Principal)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/principal.json")
    config shouldBe Test(BasicPrincipal("sammy"))
  }
})
