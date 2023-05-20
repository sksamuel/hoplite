package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import com.sksamuel.hoplite.sources.toStringMap
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.util.Properties
class SystemPropertiesPropertySourceTest : FunSpec() {

  init {

    test("reads config from string") {
      data class TestConfig(val a: String, val b: Int)

      val config = ConfigLoader {
        addPropertySource(
          SystemPropertiesPropertySource(
            systemPropertiesMap = {
              mapOf("config.override.a" to "Overridden by system prop")
            }
          )
        )
        addPropertySource(
          PropertySource.string(
            """
          a = A value
          b = 42
          """.trimIndent(), "props"
          )
        )
      }.loadConfigOrThrow<TestConfig>()

      config shouldBe TestConfig("Overridden by system prop", 42)
    }

    test("reads from real system props") {
      data class TestConfig(val a: String, val b: Int)

      try {
        // this is an error prone test because we have to remember to clear these properties after the test
        System.setProperty("config.override.a", "Overridden by system prop")
        System.setProperty("config.override.b", "42")

        // use default property sources to load system properties
        ConfigLoader().loadConfigOrThrow<TestConfig>() shouldBe TestConfig("Overridden by system prop", 42)
      } finally {
        System.clearProperty("config.override.a")
        System.clearProperty("config.override.b")
      }
    }

    test("properties toStringMap extension function ignores non-string types") {
      Properties().apply {
        setProperty("foo", "bar")
        put("name", "value") // same as setProperty when you use String args
        put(42, "int") // should be ignored because key is not a string
        put("int", listOf(42)) // should be ignored because value is not a string
      }.toStringMap() shouldContainExactly mapOf("foo" to "bar", "name" to "value")
    }
  }
}
