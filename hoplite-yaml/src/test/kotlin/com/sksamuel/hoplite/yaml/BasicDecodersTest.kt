package com.sksamuel.hoplite.yaml

import arrow.core.NonEmptyList
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

enum class Wine { Malbec, Shiraz, Merlot }

class BasicTypesTest : FunSpec({

  test("loading basic data class with primitive fields") {
    data class Test(val a: String, val b: Int, val c: Long, val d: Boolean, val e: Float, val f: Double)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test1.yml")
    config shouldBe Test(a = "Sammy", b = 1, c = 12312313123, d = true, e = 10.4F, f = 5646.54)
  }

  test("LocalDateTime") {
    data class Test(val date: LocalDateTime)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_datetime.yml")
    config shouldBe Test(LocalDateTime.of(2016, 5, 12, 12, 55, 31))
  }

  test("LocalDate") {
    data class Test(val date: LocalDate)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_date.yml")
    config shouldBe Test(LocalDate.of(2016, 5, 12))
  }

  test("Duration") {
    data class Test(val nanos: Duration,
                    val millis: Duration,
                    val seconds: Duration,
                    val hours: Duration,
                    val days: Duration)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_duration.yml")
    config shouldBe Test(
          Duration.ofNanos(10),
          Duration.ofMillis(5124),
          Duration.ofSeconds(12),
          Duration.ofHours(1),
          Duration.ofDays(3)
      )
  }

  test("kotlin.Enum") {
    data class Test(val wine: Wine)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_enum.yml")
    config shouldBe Test(Wine.Malbec)
  }

  test("UUID") {
    data class Test(val uuid: UUID)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_uuid.yml")
    config shouldBe Test(UUID.fromString("66cefa93-9816-4c09-aad9-6355664e3e4f"))
  }

  test("List<T> as delimited string") {
    data class Test(val strings: List<String>, val longs: List<Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_array_as_delimited_string.yml")
    config shouldBe Test(listOf("1", "2", "a", "b"), listOf(1, 2, 3, 4))
  }

  test("List<T>") {
    data class Test(val strings: List<String>, val longs: List<Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_array.yml")
    config shouldBe Test(listOf("1", "2", "a", "b"), listOf(1, 2, 3, 4))
  }

  test("NonEmptyList<A> as delimited string") {
    data class Test(val strings: NonEmptyList<String>, val longs: NonEmptyList<Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_array_as_delimited_string.yml")
    config shouldBe Test(NonEmptyList.of("1", "2", "a", "b"), NonEmptyList.of(1, 2, 3, 4))
  }

  test("Maps<K,V>") {
    data class Test(val map1: Map<String, Int>, val map2: Map<Int, Boolean>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_map.yml")
    config shouldBe Test(mapOf("a" to 11, "b" to 22, "c" to 33), mapOf(11 to true, 22 to false, 33 to true))
  }

  test("BigDecimal") {
    data class Test(val a: BigDecimal, val b: BigDecimal)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_bigdecimal.yml")
    config shouldBe Test(10.0.toBigDecimal(), 20.3334.toBigDecimal())
  }

  test("BigInteger") {
    data class Test(val a: BigInteger)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_biginteger.yml")
    config shouldBe Test(BigInteger.valueOf(10000L))
  }
})
