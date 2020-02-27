package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.decoder.SizeInBytes
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SizeInBytesTest : FunSpec({
  test("size in bytes units") {
    data class Test(val a: SizeInBytes, val b: SizeInBytes, val c: SizeInBytes, val d: SizeInBytes)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/size_in_bytes.json")
    config shouldBe Test(SizeInBytes(10), SizeInBytes(12000000), SizeInBytes(57671680), SizeInBytes(44000000000))
  }
})
