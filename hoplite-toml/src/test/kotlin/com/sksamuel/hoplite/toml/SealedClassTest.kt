package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SealedClassTest : FunSpec() {
  init {
    test("should decode sealed class types") {
      val actual = ConfigLoader().loadConfigOrThrow<Versions>("/versions.toml").versions
      println(actual)
      actual shouldBe mapOf(
        "version" to Version.ValueVersion("1.2.3"),
        "strict_version" to Version.StrictlyVersion("2.3.4")
      )
    }
  }
}

data class Versions(val versions: Map<String, Version>)

sealed class Version {
  abstract val value: String

  data class StrictlyVersion(val strictly: String) : Version() {
    override val value: String
      get() = strictly
  }

  data class ValueVersion(override val value: String) : Version()
}
