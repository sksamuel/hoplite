package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NullVariantsTest : FunSpec({

  // YAML 1.1/1.2 PLAIN style allows `null`, `Null`, `NULL`, and `~` as null. The hoplite YAML
  // parser only recognised the lowercase literal `null`; the others were producing StringNodes
  // containing the literal text, so a nullable String? field surprisingly came out non-null.
  test("YAML null variants should all decode to null") {
    data class Cfg(
      val a: String?,
      val b: String?,
      val c: String?,
      val d: String?,
    )

    val cfg = ConfigLoader().loadConfigOrThrow<Cfg>("/test_null_variants.yml")

    cfg shouldBe Cfg(a = null, b = null, c = null, d = null)
  }
})
