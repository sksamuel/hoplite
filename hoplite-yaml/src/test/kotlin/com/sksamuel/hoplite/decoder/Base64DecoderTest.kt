package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.ByteBuffer

class Base64DecoderTest : StringSpec({

  "base64 decoded from string" {

    data class Test(val b: Base64)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/base64.yml")
    config shouldBe Test(Base64(ByteBuffer.wrap("hello world".encodeToByteArray())))
  }

  "invalid base64" {

    data class Test(val b: Base64)

    shouldThrow<ConfigException> {
      ConfigLoaderBuilder.default()
        .addSource(YamlPropertySource("b: 123!"))
        .build()
        .loadConfigOrThrow<Test>()
    }.message shouldContain "Base64 could not be decoded from a String value: 123!"
  }
})
