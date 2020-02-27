package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from json" {
    data class Test(val file: File)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_file.json")
    config shouldBe Test(File("/home/user/sam"))
  }
})
