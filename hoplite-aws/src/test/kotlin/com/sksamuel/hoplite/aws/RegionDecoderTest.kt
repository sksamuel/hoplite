package com.sksamuel.hoplite.aws

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Pos
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DotPath
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.full.createType

class RegionDecoderTest : StringSpec() {
  init {
    "region converter" {
      RegionDecoder().safeDecode(
        StringNode("us-east-1", Pos.NoPos, DotPath.root, emptyMap()),
        Region::class.createType(),
        DecoderContext.zero
      ) shouldBe Region.getRegion(Regions.US_EAST_1).valid()

      RegionDecoder().safeDecode(
        StringNode("us-qwewqe-1", Pos.NoPos, DotPath.root, emptyMap()),
        Region::class.createType(),
        DecoderContext.zero
      ) shouldBe
        ConfigFailure.Generic("Cannot create region from us-qwewqe-1").invalid()
    }
  }
}
