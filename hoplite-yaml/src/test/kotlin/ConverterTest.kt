package com.sksamuel.hoplite.yaml

import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.time.LocalDate
import java.time.LocalDateTime

enum class Wine { Malbec, Shiraz, Merlot }

class ConverterTest : FunSpec({

  test("loading basic data class with primitive fields") {
    data class Test(val a: String, val b: Int, val c: Long, val d: Boolean, val e: Float, val f: Double)
    loadConfig<Test>("/test1.yml").shouldBeValid {
      it.a shouldBe Test(a = "Sammy", b = 1, c = 12312313123, d = true, e = 10.4F, f = 5646.54)
    }
  }

  test("LocalDateTime support") {
    data class Test(val date: LocalDateTime)
    loadConfig<Test>("/test_datetime.yml").shouldBeValid {
      it.a shouldBe Test(LocalDateTime.of(2016, 5, 12, 12, 55, 31))
    }
  }

  test("LocalDate support") {
    data class Test(val date: LocalDate)
    loadConfig<Test>("/test_date.yml").shouldBeValid {
      it.a shouldBe Test(LocalDate.of(2016, 5, 12))
    }
  }

  test("Enum support") {
    data class Test(val wine: Wine)
    loadConfig<Test>("/test_enum.yml").shouldBeValid {
      it.a shouldBe Test(Wine.Malbec)
    }
  }
})