package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from yaml" {
    data class Test(val file: File)
    ConfigLoader().loadConfig<Test>("/test_file.yml").shouldBeValid {
      it.a shouldBe Test(File("/home/user/sam"))
    }
  }
})