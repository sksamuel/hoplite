package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.Properties

@OptIn(ExperimentalHoplite::class)
class SystemContextResolverTest : FunSpec({


  test("should support processors") {

    data class Config(val foo: String)

    val props = Properties()
    props["foo"] = "boaty \${{system:processors}} face"

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(foo = "boaty ${Runtime.getRuntime().availableProcessors()} face")
  }

  test("should support timestamp") {

    data class Config(val foo: Long)

    val props = Properties()
    props["foo"] = "\${{system:timestamp}}"

    val config = ConfigLoaderBuilder.newBuilder()
      .addPropertySource(PropsPropertySource(props))
      .build()
      .loadConfigOrThrow<Config>()

    config.foo.shouldBeGreaterThan(Instant.now().minusSeconds(2).toEpochMilli())
    config.foo.shouldBeLessThan(Instant.now().plusSeconds(2).toEpochMilli())
  }

})
