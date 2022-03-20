package com.sksamuel.hoplite.report

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultObfuscatorTest : FunSpec({

  test("happy path") {
    DefaultObfuscator.obfuscate("foo").shouldBe("foo*****")
    DefaultObfuscator.obfuscate("foobar").shouldBe("foo*****")
    DefaultObfuscator.obfuscate("foobarfoobar").shouldBe("foo*****")
    DefaultObfuscator.obfuscate("f").shouldBe("f*****")
    DefaultObfuscator.obfuscate("").shouldBe("*****")
  }

})
