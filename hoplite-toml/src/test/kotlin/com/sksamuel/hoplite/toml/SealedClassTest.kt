package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SealedClassTest : FunSpec() {
  init {

    test("should decode sealed class types when a subclass has a single string field") {
      val actual = ConfigLoader().loadConfigOrThrow<Versions>("/versions.toml").versions
      actual shouldBe mapOf(
        "version" to Version.ValueVersion("1.2.3"),
        "strict_version" to Version.StrictlyVersion("2.3.4")
      )
    }

    test("should include types when failing") {
      shouldThrowAny {
        ConfigLoader().loadConfigOrThrow<Versions>("/versions2.toml").versions
      }.message
        .shouldContain("Tried com.sksamuel.hoplite.toml.Version\$StrictlyVersion, com.sksamuel.hoplite.toml.Version\$ValueVersion")
        .shouldContain("Collection element decode failure")
        .shouldContain("Could not find appropriate subclass of class com.sksamuel.hoplite.toml.Version")
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
