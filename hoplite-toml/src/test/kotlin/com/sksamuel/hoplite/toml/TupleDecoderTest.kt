package com.sksamuel.hoplite.toml

import arrow.core.Tuple2
import arrow.core.Tuple3
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

class TupleDecoderTest : StringSpec({
  "tuples decoded from toml" {
    data class Test(val a: Tuple2<String, Int>, val b: Tuple3<Double, Boolean, UUID>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.toml")
    config shouldBe Test(Tuple2("hello", 4), Tuple3(6.5, true, UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d")))
  }
})
