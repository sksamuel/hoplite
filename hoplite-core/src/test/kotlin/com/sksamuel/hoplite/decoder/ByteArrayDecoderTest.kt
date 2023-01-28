package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ByteArrayDecoderTest : StringSpec({

  "byte array decoder should decode strings" {
    data class Test(val a: ByteArray)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_byte_array.props")
    config.a shouldBe byteArrayOf(104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100)
  }
})
