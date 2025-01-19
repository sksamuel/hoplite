package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addCommandLineSource
import com.sksamuel.hoplite.addEnvironmentSource
import com.sksamuel.hoplite.addResourceOrFileSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class DenormalizedMapKeysTest : FunSpec({
  data class Foo(
    val xVal: String = "x"
  )

  data class MapContainer(
    val m: Map<String, Foo> = emptyMap()
  )

  test("should set denormalized map keys and decode a data class inside a map") {
    val config = ConfigLoaderBuilder.default()
      .addResourceOrFileSource("/test_data_class_in_map.yaml")
      .build()
      .loadConfigOrThrow<MapContainer>()

    config shouldBe MapContainer(
      m = mapOf(
        "DC1" to Foo("10"),
        "DC2" to Foo("20"),
      )
    )
  }

  test("should set denormalized map keys for CLI arguments") {
    val config = ConfigLoaderBuilder.default()
      .addCommandLineSource(
        arrayOf(
          "--m.DC1.x-val=10",
          "--m.DC2.x-val=20",
        ),
        prefix = "--",
        delimiter = "="
      )
      .build()
      .loadConfigOrThrow<MapContainer>()

    config shouldBe MapContainer(
      m = mapOf(
        "DC1" to Foo("10"),
        "DC2" to Foo("20"),
      )
    )
  }

  test("should set denormalized map keys for CLI arguments, overriding a property source") {
    val config = ConfigLoaderBuilder.default()
      .addCommandLineSource(
        arrayOf(
          "--m.DC1.x-val=15",
          "--m.DC2.x-val=25",
        ),
        prefix = "--",
        delimiter = "="
      )
      .addResourceOrFileSource("/test_data_class_in_map.yaml")
      .build()
      .loadConfigOrThrow<MapContainer>()

    config shouldBe MapContainer(
      m = mapOf(
        "DC1" to Foo("15"),
        "DC2" to Foo("25"),
      )
    )
  }

  test("should set denormalized map keys from environment variables") {
    withEnvironment(
      mapOf(
        "m_DC1_x-val" to "15",
        "m_DC2_x-val" to "25"
      )
    ) {
      val config = ConfigLoader()
        .loadConfigOrThrow<MapContainer>()

      config shouldBe MapContainer(
        m = mapOf(
          "DC1" to Foo("15"),
          "DC2" to Foo("25"),
        )
      )
    }
  }

  test("should set denormalized map keys from environment variables, overriding a property source") {
    withEnvironment(
      mapOf(
        "m_DC1_x-val" to "15",
        "m_DC2_x-val" to "25"
      )
    ) {
      val config = ConfigLoader()
        .loadConfigOrThrow<MapContainer>("/test_data_class_in_map.yaml")

      config shouldBe MapContainer(
        m = mapOf(
          "DC1" to Foo("15"),
          "DC2" to Foo("25"),
        )
      )
    }
  }

  test("should set denormalized map keys from command line arguments, overriding environment variables and property sources") {
    withEnvironment(
      mapOf(
        "m_DC1_x-val" to "15",
        "m_DC2_x-val" to "25"
      )
    ) {
      val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addCommandLineSource(
          arrayOf(
            "--m.DC1.x-val=20",
            "--m.DC2.x-val=30",
          ),
        )
        .addEnvironmentSource()
        .addResourceOrFileSource("/test_data_class_in_map.yaml")
        .build()
        .loadConfigOrThrow<MapContainer>()

      config shouldBe MapContainer(
        m = mapOf(
          "DC1" to Foo("20"),
          "DC2" to Foo("30"),
        )
      )
    }
  }

  test("should set denormalized map keys from command line arguments with the CLI case") {
    val config = ConfigLoaderBuilder.default()
      .addCommandLineSource(
        arrayOf(
          "--m.Dc1.x-val=20",
          "--m.Dc2.x-val=30",
        ),
      )
      .addResourceOrFileSource("/test_data_class_in_map.yaml")
      .build()
      .loadConfigOrThrow<MapContainer>()

    config shouldBe MapContainer(
      m = mapOf(
        "Dc1" to Foo("20"),
        "Dc2" to Foo("30"),
      )
    )
  }
})
