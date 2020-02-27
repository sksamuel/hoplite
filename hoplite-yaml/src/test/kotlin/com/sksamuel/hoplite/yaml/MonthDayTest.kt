package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec
import java.time.MonthDay

class MonthDayTest : StringSpec({
  "MonthDay decoded from YAML" {
    data class Test(val a: MonthDay, val b: MonthDay)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/month_day.yml")
    config shouldBe Test(MonthDay.of(12, 30), MonthDay.of(4, 15))
  }
})
