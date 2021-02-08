package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.time.Instant


class InstantTest : StringSpec({
  "java.time.Instant decoded from YAML" {
    data class Config(val instant: Instant)

    val config = ConfigLoader().loadConfigOrThrow<Config>("/test_instant.yml")
    config shouldBe Config(Instant.parse("2015-03-14T09:26:53.590Z"))
  }
})
