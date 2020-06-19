package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.math.BigInteger

class BasicDecodersTest : FunSpec({

  test("BigDecimal") {
    data class Test(val a: BigDecimal, val b: BigDecimal, val c: BigDecimal, val d: BigDecimal)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_bigdecimal.json")
    config shouldBe Test(10.toBigDecimal(), 20.3334.toBigDecimal(), 10.2.toBigDecimal(), 20.3334.toBigDecimal())
  }

  test("BigInteger") {
    data class Test(val a: BigInteger, val b: BigInteger)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_biginteger.json")
      config shouldBe Test(10000.toBigInteger(), 1231412412.toBigInteger())
  }

})
