package com.sksamuel.hoplite.yaml

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

  test("should set denormalized map keys from environment variables") {
    withEnvironment(
      mapOf(
        "m.DC1.x-val" to "15",
        "m.DC2.x-val" to "25"
      )
    ) {
      val config = ConfigLoaderBuilder.default()
        .addEnvironmentSource()
        .addResourceOrFileSource("/test_data_class_in_map.yaml")
        .build()
        .loadConfigOrThrow<MapContainer>()

      config shouldBe MapContainer(
        m = mapOf(
          "DC1" to Foo("15"),
          "DC2" to Foo("25"),
        )
      )
      /*
       but actually, it is:
       {
        "dc2" = Foo(xVal=25),
        "dc1" = Foo(xVal=15)
       }
       */
    }
  }
})
