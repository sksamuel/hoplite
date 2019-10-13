package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val file: File)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_file.json")
    config shouldBe Test(File("/home/user/sam"))
  }
})
