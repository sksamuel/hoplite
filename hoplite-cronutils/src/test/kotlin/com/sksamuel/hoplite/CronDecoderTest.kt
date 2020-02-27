package com.sksamuel.hoplite

import com.cronutils.model.Cron
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class CronDecoderTest : StringSpec() {
  init {
    "cron decoder" {
      data class Config(val a: Cron)
      ConfigLoader().loadConfigOrThrow<Config>("/cron.props").a.asString() shouldBe "0 0 * * *"
    }
  }
}
