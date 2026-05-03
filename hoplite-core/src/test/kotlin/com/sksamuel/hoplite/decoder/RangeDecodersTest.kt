package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RangeDecodersTest : FunSpec({

  // The previous regex required digits only, so `-5..5` failed to decode even though IntRange and
  // LongRange both happily represent ranges with negative endpoints.
  test("IntRange should accept negative endpoints") {
    data class Cfg(val r: IntRange)
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("r" to "-5..5"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.r shouldBe IntRange(-5, 5)
  }

  test("IntRange should accept both endpoints negative") {
    data class Cfg(val r: IntRange)
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("r" to "-10..-5"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.r shouldBe IntRange(-10, -5)
  }

  test("LongRange should accept negative endpoints") {
    data class Cfg(val r: LongRange)
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("r" to "-1000..1000"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.r shouldBe LongRange(-1000L, 1000L)
  }

  test("positive ranges still decode") {
    data class Cfg(val r: IntRange)
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("r" to "1..10"))
      .build()
      .loadConfigOrThrow<Cfg>()
    cfg.r shouldBe IntRange(1, 10)
  }
})
