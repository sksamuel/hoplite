package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

class PropsDecoderTest : FunSpec({
  test("props should be supported as a nested type") {
    data class Config(val myprops: Properties)

    val expected = Properties()
    expected["foo"] = "wibble"
    expected["bar"] = "wobble"

    ConfigLoaderBuilder.default()
      .addResourceSource("/props_decoder.yml")
      .build()
      .loadConfig<Config>().getUnsafe() shouldBe Config(expected)
  }
})
