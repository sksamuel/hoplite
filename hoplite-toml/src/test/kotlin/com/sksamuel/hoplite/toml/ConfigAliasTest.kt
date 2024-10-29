package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigAliasTest : FunSpec({
  data class A(
    @ConfigAlias("b") @ConfigAlias("zah") val bb: String,
    val c: Int
  )

  data class D(
    val e: String,
    @ConfigAlias("f") val ff: Boolean
  )

  data class AliasConfig(
    val a: A,
    @ConfigAlias("d") val dd: D
  )

  test("parsers should support @ConfigAlias") {
    val config = ConfigLoader().loadConfigOrThrow<AliasConfig>("/alias.toml")
    config.a.bb shouldBe "Tom Preston-Werner"
    config.a.c shouldBe 5000
    config.dd.e shouldBe "192.168.1.1"
    config.dd.ff shouldBe true
  }

  test("parsers should support multiple @ConfigAlias") {
    val config = ConfigLoader().loadConfigOrThrow<AliasConfig>("/repeated_alias.toml")
    config.a.bb shouldBe "Tom Preston-Werner"
    config.a.c shouldBe 5000
    config.dd.e shouldBe "192.168.1.1"
    config.dd.ff shouldBe true
  }
})
