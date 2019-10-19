package com.sksamuel.hoplite.aws

import arrow.core.invalid
import arrow.core.valid
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.reflect.full.createType

class RegionDecoderTest : StringSpec() {
  init {
    "region converter" {
      RegionDecoder().safeDecode(
        StringNode("us-east-1", Pos.NoPos),
        Region::class.createType(),
        DecoderContext.zero
      ) shouldBe Region.getRegion(Regions.US_EAST_1).valid()

      RegionDecoder().safeDecode(
        StringNode("us-qwewqe-1", Pos.NoPos),
        Region::class.createType(),
        DecoderContext.zero
      ) shouldBe
        ConfigFailure.Generic("Cannot create region from us-qwewqe-1").invalid()
    }
  }
}
