package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import java.nio.file.Paths

class PathDecoderTest : StringSpec({
  "Path decoded from json" {
    data class Test(val path: Path)

    val config = ConfigLoaderBuilder.defaultWithoutPropertySources().build()
      .loadConfigOrThrow<Test>("/test_path.json")
    config shouldBe Test(Paths.get("/home/user/sam"))
  }
})
