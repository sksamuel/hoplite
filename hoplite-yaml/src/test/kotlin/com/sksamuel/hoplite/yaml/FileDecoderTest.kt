package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from yaml" {
    data class Test(val file: File)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_file.yml")
    config shouldBe Test(File("/home/user/sam"))
  }
})
