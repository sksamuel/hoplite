package com.sksamuel.hoplite.resolver.context

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch

@OptIn(ExperimentalHoplite::class)
class RandomContextResolverTest : FunSpec({

  fun loader(map: Map<String, Any>) = ConfigLoaderBuilder.newBuilderWithoutPropertySources()
    .addMapSource(map)
    .build()

  // The simple paths (int / long / double / uuid / boolean) had always worked because they hit
  // the literal `when (path)` branch. The parameterised paths went through regexes that were
  // anchored to the outer `${random.int(N)}` form, so they never matched the inner path string
  // and the placeholders were left unresolved.
  test("\${{ random:int(N) }} returns an int in [0, N)") {
    data class Cfg(val v: Int)
    val cfg = loader(mapOf("v" to "\${{ random:int(10) }}")).loadConfigOrThrow<Cfg>()
    cfg.v.shouldBeInRange(0..9)
  }

  test("\${{ random:int(M, N) }} returns an int in [M, N)") {
    data class Cfg(val v: Int)
    val cfg = loader(mapOf("v" to "\${{ random:int(100, 200) }}")).loadConfigOrThrow<Cfg>()
    cfg.v.shouldBeGreaterThanOrEqual(100)
    cfg.v.shouldBeLessThan(200)
  }

  test("\${{ random:string(N) }} returns a lower-case alpha string of length N") {
    data class Cfg(val v: String)
    val cfg = loader(mapOf("v" to "\${{ random:string(12) }}")).loadConfigOrThrow<Cfg>()
    cfg.v shouldMatch Regex("^[a-z]{12}$")
  }

  // Smoke test for the simple paths so all the random-context paths are covered together.
  test("\${{ random:boolean }} returns a boolean") {
    data class Cfg(val v: Boolean)
    val cfg = loader(mapOf("v" to "\${{ random:boolean }}")).loadConfigOrThrow<Cfg>()
    (cfg.v == true || cfg.v == false) shouldBe true
  }
})
