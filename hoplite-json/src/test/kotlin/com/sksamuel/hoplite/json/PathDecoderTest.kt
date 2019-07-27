package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Path
import java.nio.file.Paths

class PathDecoderTest : StringSpec({
  "Path decoded from json" {
    data class Test(val path: Path)
    ConfigLoader(Json).loadConfig<Test>("/test_path.json").shouldBeValid {
      it.a shouldBe Test(Paths.get("/home/user/sam"))
    }
  }
})