package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import java.nio.file.Paths

class PathDecoderTest : StringSpec({
  "Path decoded from yaml" {
    data class Test(val path: Path)

    val config = ConfigLoaderBuilder.defaultWithoutPropertySources().build()
      .loadConfigOrThrow<Test>("/test_path.yml")
    config shouldBe Test(Paths.get("/home/user/sam"))
  }
})
