package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class SystemContextResolverTest : FunSpec({

  data class Config(val foo: String)

  test("should support processors") {

    val props = Properties()
    props["foo"] = "boaty \${{system:processors}} face"

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(foo = "boaty ${Runtime.getRuntime().availableProcessors()} face")
  }

})
