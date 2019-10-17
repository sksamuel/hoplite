package com.sksamuel.hoplite.json

import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.Tuple4
import arrow.core.Tuple5
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*

inline class Port(val value: Int)

class TupleDecoderTest : StringSpec({

  "tuples 2 and 3decoded from json" {
    data class Test(val a: Tuple2<String, Int>, val b: Tuple3<Double, Boolean, UUID>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuples.json")
    config shouldBe Test(Tuple2("hello", 4), Tuple3(6.5, true, UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d")))
  }

  "tuple 4 from json" {
    data class Test(val a: Tuple4<Double, Boolean, UUID, Port>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuple_4.json")
    config shouldBe Test(Tuple4(6.5, true, UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d"), Port(9200)))
  }

  "tuple 5 from json" {
    data class Test(val a: Tuple5<Double, Boolean, UUID, Port, String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_tuple_5.json")
    config shouldBe Test(Tuple5(6.5,
      true,
      UUID.fromString("383d27c5-d087-4d36-b4c4-6dd7defe088d"),
      Port(9200),
      "hello"))
  }
})
