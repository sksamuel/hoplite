package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.Tuple4
import io.vavr.Tuple5
import io.vavr.kotlin.tuple
import java.util.*

class TuplesTest : FunSpec({

  test("Tuple2<String, String> decoded from yaml") {
    data class Test(val tuple2: Tuple2<String, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.yml")
    config shouldBe Test(tuple("test1", "test2"))
  }

  test("Tuple3<String, Int, String> decoded from yaml") {
    data class Test(val tuple3: Tuple3<String, Int, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.yml")
    config shouldBe Test(tuple("test1", 1, "test2"))
  }

  test("Tuple4<Int, Int, Int, Int> decoded from yaml") {
    data class Test(val tuple4: Tuple4<Int, Int, Int, Int>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.yml")
    config shouldBe Test(tuple(1, 2, 3, 4))
  }

  test("Tuple5<Boolean, String, Int, Int, UUID> decoded from yaml") {
    data class Test(val tuple5: Tuple5<Boolean, String, Int, Int, UUID>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.yml")
    config shouldBe Test(tuple(true, "test", 1, 2, UUID.fromString("3c7799c0-3e3a-11eb-b378-0242ac130002")))
  }
})
