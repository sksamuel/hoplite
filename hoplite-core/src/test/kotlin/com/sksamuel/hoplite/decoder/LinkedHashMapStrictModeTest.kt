package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addMapSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LinkedHashMapStrictModeTest : FunSpec({

  // MapDecoder explicitly calls context.usedPaths.add(v.path) for each value, so strict mode
  // doesn't flag the child entries as unused. LinkedHashMapDecoder forgot to do the same — so
  // strict() incorrectly reported every entry of a LinkedHashMap field as unused, even though
  // every entry was decoded into the resulting value.
  test("strict mode should accept all entries of a LinkedHashMap") {
    data class Cfg(val m: LinkedHashMap<String, String>)

    val cfg = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("m.a" to "1", "m.b" to "2", "m.c" to "3"))
      .strict()
      .build()
      .loadConfigOrThrow<Cfg>()

    cfg.m shouldBe linkedMapOf("a" to "1", "b" to "2", "c" to "3")
  }
})
