package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeBetween

class RandomPreprocessorTest : FunSpec({

  // RandomPreprocessor.doubleRule was substituting Random.nextLong() instead of nextDouble(),
  // so `${random.double}` produced an integer (rejected by Double decoders that strict-parse,
  // and semantically wrong for code that relied on a uniform [0, 1) value).
  test("\${random.double} should produce a value in [0, 1)") {
    data class Cfg(val v: Double)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("v" to "\${random.double}"))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.v.shouldBeBetween(0.0, 1.0, 0.0)
  }
})
