package com.sksamuel.hoplite.aws

import arrow.data.invalidNel
import arrow.data.valid
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class RegionDecoderProviderTest : StringSpec() {
  init {
    "region converter" {
      RegionDecoder().decode(StringNode("us-east-1", Pos.NoPos)) shouldBe Region.getRegion(Regions.US_EAST_1).valid()
      RegionDecoder().decode(StringNode("qwewqe-1", Pos.NoPos)) shouldBe ConfigFailure("Cannot create region from qwewqe-1").invalidNel()
    }
  }
}