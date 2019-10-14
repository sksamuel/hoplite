package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Path
import java.nio.file.Paths

class PathDecoderTest : StringSpec({
  "Path decoded from yaml" {
    data class Test(val path: Path)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_path.yml")
    config shouldBe Test(Paths.get("/home/user/sam"))
  }
})
