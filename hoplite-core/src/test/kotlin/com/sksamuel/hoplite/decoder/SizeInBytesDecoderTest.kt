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
})
