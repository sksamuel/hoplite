package com.sksamuel.hoplite.report

import com.sksamuel.hoplite.decoder.DotPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DefaultObfuscatorTest : FunSpec({

  test("happy path") {
    DefaultObfuscator.obfuscate(DotPath.root, "foo").shouldBe("foo*****")
    DefaultObfuscator.obfuscate(DotPath.root, "foobar").shouldBe("foo*****")
    DefaultObfuscator.obfuscate(DotPath.root, "foobarfoobar").shouldBe("foo*****")
    DefaultObfuscator.obfuscate(DotPath.root, "f").shouldBe("f*****")
    DefaultObfuscator.obfuscate(DotPath.root, "").shouldBe("*****")
  }

})
