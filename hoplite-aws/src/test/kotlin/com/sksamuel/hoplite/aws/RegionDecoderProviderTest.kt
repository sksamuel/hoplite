package com.sksamuel.hoplite.aws

import arrow.data.invalidNel
import arrow.data.valid
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DecoderRegistry
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.reflect.full.createType

class RegionDecoderProviderTest : StringSpec() {
  init {
    "region converter" {
      RegionDecoder().safeDecode(
        StringNode("us-east-1", Pos.NoPos, dotpath = ""),
        Region::class.createType(),
        DecoderRegistry.zero,
        ""
      ) shouldBe Region.getRegion(Regions.US_EAST_1).valid()

      RegionDecoder().safeDecode(
        StringNode("us-qwewqe-1", Pos.NoPos, dotpath = ""),
        Region::class.createType(),
        DecoderRegistry.zero,
        ""
      ) shouldBe
        ConfigFailure.Generic("Cannot create region from us-qwewqe-1").invalidNel()
    }
  }
}
