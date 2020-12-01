package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class SetDecoderTest : StringSpec({

  "Set decoded from yml file" {
    data class Test(val a: Set<Long>, val b: Set<String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/sets.yml")
    config shouldBe Test(setOf(1, 2, 3), setOf("1", "2"))
    config.a.shouldBeInstanceOf<HashSet<*>>()
  }

  "Set of Kotlin.Enum decoded from yml file" {
    data class TestEnum(val wines: Set<Wine>)

    val config = ConfigLoader().loadConfigOrThrow<TestEnum>("/test_set_enum.yml")
    config shouldBe TestEnum(setOf(Wine.Malbec, Wine.Shiraz))
  }
})
