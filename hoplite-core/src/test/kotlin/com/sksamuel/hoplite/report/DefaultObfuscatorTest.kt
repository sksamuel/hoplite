package com.sksamuel.hoplite.report

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultObfuscatorTest : FunSpec({

  test("happy path") {
    DefaultObfuscator.obfuscate("foo").shouldBe("fo*****")
    DefaultObfuscator.obfuscate("foobar").shouldBe("fo*****")
    DefaultObfuscator.obfuscate("foobarfoobar").shouldBe("fo*****")
    DefaultObfuscator.obfuscate("f").shouldBe("f*****")
    DefaultObfuscator.obfuscate("").shouldBe("*****")
  }

})
