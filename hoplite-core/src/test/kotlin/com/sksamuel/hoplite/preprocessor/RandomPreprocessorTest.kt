package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.string.shouldContain

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

  // Random.nextInt(0, max) and Random.nextInt(min, max) throw IllegalArgumentException for
  // empty ranges. The regex permits "0", so `${random.int(0)}` previously crashed deep inside
  // the loader with an opaque IllegalArgumentException. Leave the placeholder verbatim so the
  // user gets the standard "Unresolved substitution" failure pointing at their typo instead.
  test("\${random.int(0)} surfaces an unresolved-substitution failure, not IllegalArgumentException") {
    data class Cfg(val v: String)

    val ex = io.kotest.assertions.throwables.shouldThrow<com.sksamuel.hoplite.ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addMapSource(mapOf("v" to "\${random.int(0)}"))
        .build()
        .loadConfigOrThrow<Cfg>()
    }
    ex.message.orEmpty() shouldContain "\${random.int(0)}"
  }

  test("\${random.int(5,5)} (empty range) surfaces an unresolved-substitution failure") {
    data class Cfg(val v: String)

    val ex = io.kotest.assertions.throwables.shouldThrow<com.sksamuel.hoplite.ConfigException> {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addMapSource(mapOf("v" to "\${random.int(5,5)}"))
        .build()
        .loadConfigOrThrow<Cfg>()
    }
    ex.message.orEmpty() shouldContain "\${random.int(5,5)}"
  }
})
