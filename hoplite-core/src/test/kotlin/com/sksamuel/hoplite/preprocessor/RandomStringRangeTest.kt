package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.string.shouldMatch
import kotlin.random.Random

class RandomStringRangeTest : FunSpec({

  // Random.nextInt(from, until) is exclusive on `until`, so the previous implementation generated
  // chars in [a, z) — never 'z'. Probabilistically, 50 strings of length 50 should contain 'z'
  // in essentially every run when the upper bound is correct (P(no z) ≈ (25/26)^2500 ≈ 1e-43).
  test("\${random.string(N)} should be able to generate 'z'") {
    data class Cfg(val v: String)

    // Use a fixed seed-equivalent: generate enough chars that 'z' must appear if the bound is
    // inclusive. Run via the loader many times.
    val all = buildString {
      // Use Kotlin Random with a fixed seed so the test is deterministic but exercises a wide
      // range. Without the fix, 'z' is impossible regardless of run count.
      val rng = Random(12345)
      repeat(2500) { append(('a'.code + rng.nextInt(0, 26)).toChar()) }
    }
    // Sanity check our test setup — with the inclusive [a, z] range, this string should contain 'z'.
    all.toCharArray().toSet() shouldContain 'z'

    // Now exercise the loader: a single 1000-char string from the resolver should also contain 'z'
    // with overwhelming probability. We pick a fixed length large enough that the probability of
    // not seeing 'z' under the corrected bound is < 1 in 10^17.
    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("v" to "\${random.string(1000)}"))
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.v shouldMatch Regex("^[a-z]{1000}$")
    cfg.v.toCharArray().toSet() shouldContain 'z'
  }
})
