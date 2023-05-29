package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class ManifestContextResolverTest : FunSpec({

  data class Config(val foo: String)

  test("should support manifest lookup") {

    val props = Properties()
    props["foo"] = "boaty \${{manifest:Manifest-Version}} face"

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(foo = "boaty 1.0 face")
  }
})
