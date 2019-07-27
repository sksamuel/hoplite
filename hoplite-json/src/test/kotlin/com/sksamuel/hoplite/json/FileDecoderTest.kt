package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val file: File)
    ConfigLoader().loadConfig<Test>("/test_file.json").shouldBeValid {
      it.a shouldBe Test(File("/home/user/sam"))
    }
  }
})