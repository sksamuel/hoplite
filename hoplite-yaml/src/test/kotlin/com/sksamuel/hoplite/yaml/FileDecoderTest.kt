package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class FileDecoderTest : StringSpec({
  "file decoded from yaml" {
    data class Test(val file: File)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_file.yml")
    config shouldBe Test(File("/home/user/sam"))
  }
})
