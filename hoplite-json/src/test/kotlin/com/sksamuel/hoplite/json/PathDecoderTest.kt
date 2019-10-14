package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Path
import java.nio.file.Paths

class PathDecoderTest : StringSpec({
  "Path decoded from json" {
    data class Test(val path: Path)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_path.json")
    config shouldBe Test(Paths.get("/home/user/sam"))
  }
})
