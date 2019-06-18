package com.sksamuel.hoplite.yaml

import arrow.data.NonEmptyList
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

enum class Wine { Malbec, Shiraz, Merlot }

class ConverterTest : FunSpec({

  test("loading basic data class with primitive fields") {
    data class Test(val a: String, val b: Int, val c: Long, val d: Boolean, val e: Float, val f: Double)
    ConfigLoader().loadConfig<Test>("/test1.yml").shouldBeValid {
      it.a shouldBe Test(a = "Sammy", b = 1, c = 12312313123, d = true, e = 10.4F, f = 5646.54)
    }
  }

  test("LocalDateTime") {
    data class Test(val date: LocalDateTime)
    ConfigLoader().loadConfig<Test>("/test_datetime.yml").shouldBeValid {
      it.a shouldBe Test(LocalDateTime.of(2016, 5, 12, 12, 55, 31))
    }
  }

  test("LocalDate") {
    data class Test(val date: LocalDate)
    ConfigLoader().loadConfig<Test>("/test_date.yml").shouldBeValid {
      it.a shouldBe Test(LocalDate.of(2016, 5, 12))
    }
  }

  test("Duration") {
    data class Test(val nanos: Duration,
                    val millis: Duration,
                    val seconds: Duration,
                    val hours: Duration,
                    val days: Duration)
    ConfigLoader().loadConfig<Test>("/test_duration.yml").shouldBeValid {
      it.a shouldBe Test(
          Duration.ofNanos(10),
          Duration.ofMillis(5124),
          Duration.ofSeconds(12),
          Duration.ofHours(1),
          Duration.ofDays(3)
      )
    }
  }

  test("kotlin.Enum") {
    data class Test(val wine: Wine)
    ConfigLoader().loadConfig<Test>("/test_enum.yml").shouldBeValid {
      it.a shouldBe Test(Wine.Malbec)
    }
  }

  test("UUID") {
    data class Test(val uuid: UUID)
    ConfigLoader().loadConfig<Test>("/test_uuid.yml").shouldBeValid {
      it.a shouldBe Test(UUID.fromString("66cefa93-9816-4c09-aad9-6355664e3e4f"))
    }
  }

  test("List<T>") {
    data class Test(val strings: List<String>, val longs: List<Long>)
    ConfigLoader().loadConfig<Test>("/test_array.yml").shouldBeValid {
      it.a shouldBe Test(listOf("1", "2", "a", "b"), listOf(1, 2, 3, 4))
    }
  }

  test("NonEmptyList<T>") {
    data class Test(val strings: NonEmptyList<String>, val longs: NonEmptyList<Long>)
    ConfigLoader().loadConfig<Test>("/test_array.yml").shouldBeValid {
      it.a shouldBe Test(NonEmptyList.of("1", "2", "a", "b"), NonEmptyList.of(1, 2, 3, 4))
    }
  }
})