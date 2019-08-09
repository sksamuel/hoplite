package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class DashSnakeCaseMapperTest : WordSpec() {
  init {
    "standard key mappers" should {
      "support mixed dash and snake case" {
        data class Tic(val tacToe: String, val jamJar: Double)
        data class Test(val wibbleWobble: String, val tic: Tic, val jeanLuc: String)
        ConfigLoader().loadConfigOrThrow<Test>("/mixed_snake_dash_case.conf") shouldBe
          Test(wibbleWobble = "hello", tic = Tic(tacToe = "jumble", jamJar = 5.4), jeanLuc = "picard")
      }
    }
  }
}
