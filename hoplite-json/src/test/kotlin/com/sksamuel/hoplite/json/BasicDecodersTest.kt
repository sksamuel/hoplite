package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import java.math.BigDecimal
import java.math.BigInteger

class BasicDecodersTest : FunSpec({

  test("BigDecimal") {
    data class Test(val a: BigDecimal, val b: BigDecimal, val c: BigDecimal, val d: BigDecimal)
    ConfigLoader(Json).loadConfig<Test>("/test_bigdecimal.json").shouldBeValid {
      it.a shouldBe Test(10.toBigDecimal(), 20.3334.toBigDecimal(), 10.2.toBigDecimal(), 20.3334.toBigDecimal())
    }
  }

  test("BigInteger") {
    data class Test(val a: BigInteger, val b: BigInteger)
    ConfigLoader(Json).loadConfig<Test>("/test_biginteger.json").shouldBeValid {
      it.a shouldBe Test(10000.toBigInteger(), 1231412412.toBigInteger())
    }
  }

})