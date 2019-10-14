package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class KebabSnakeCaseMapperTest : WordSpec() {

  init {
    "standard param mappers" should {
      "support kebab and snake case in the same file" {
        data class Tic(val tacToe: String, val jamJar: Double)
        data class Test(val wibbleWobble: String, val tic: Tic, val jeanLucPicard: String)
        ConfigLoader().loadConfigOrThrow<Test>("/mixed_snake_dash_case.conf") shouldBe
          Test(wibbleWobble = "hello", tic = Tic(tacToe = "jumble", jamJar = 5.4), jeanLucPicard = "captain")
      }
    }
  }

}
