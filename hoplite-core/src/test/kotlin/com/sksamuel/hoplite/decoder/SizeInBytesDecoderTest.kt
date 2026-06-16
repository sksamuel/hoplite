package com.sksamuel.hoplite.decoder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SizeInBytesDecoderTest : FunSpec({
  val units = listOf(
    InformationUnit.Kilobytes,
    InformationUnit.Megabytes,
    InformationUnit.Gigabytes,

    InformationUnit.Octets,
    InformationUnit.Kibibytes,
    InformationUnit.Mebibytes,
    InformationUnit.Gibibytes
  )

  test("conversion between bytes and other units") {
    val size = 8
    for (unit in units) {
      SizeInBytes((size * unit.ratioToPrimary).toLong()).convert(unit) shouldBe size
    }
  }

  // Anchors the actual definition of each unit, independent of the round-trip test
  // above (which would happily round-trip with a wrong constant in both directions).
  test("each unit has the correct ratio to one byte") {
    InformationUnit.Bytes.ratioToPrimary shouldBe 1.0
    InformationUnit.Octets.ratioToPrimary shouldBe 1.0   // 1 octet == 8 bits == 1 byte
    InformationUnit.Bits.ratioToPrimary shouldBe 0.125   // 1 bit == 1/8 byte
    InformationUnit.Kilobytes.ratioToPrimary shouldBe 1000.0
    InformationUnit.Kibibytes.ratioToPrimary shouldBe 1024.0
  }

  test("parse converts string-with-unit to byte count") {
    SizeInBytes.parse("5B")?.size shouldBe 5L
    SizeInBytes.parse("5o")?.size shouldBe 5L  // previously yielded 0 because octets had ratio 1/8
    SizeInBytes.parse("8bit")?.size shouldBe 1L
    SizeInBytes.parse("2KB")?.size shouldBe 2000L
    SizeInBytes.parse("2KiB")?.size shouldBe 2048L
  }

  test("octets() returns the same count as bytes() since 1 octet == 1 byte") {
    val size = SizeInBytes(64)
    size.octets() shouldBe size.bytes()
  }
})
