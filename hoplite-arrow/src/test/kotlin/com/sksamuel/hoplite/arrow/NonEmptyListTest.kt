package com.sksamuel.hoplite.arrow

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NonEmptyListTest : FunSpec({

  test("NonEmptyList<A> as delimited string") {
    data class Test(val strings: arrow.core.NonEmptyList<String>, val longs: arrow.core.NonEmptyList<Long>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_nel.yml")
    config shouldBe Test(arrow.core.NonEmptyList.of("1", "2", "a", "b"), arrow.core.NonEmptyList.of(1L, 2L, 3L, 4L))
  }

})
