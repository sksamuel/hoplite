package com.sksamuel.hoplite.decoder.vavr

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.collection.List
import io.vavr.kotlin.list

class ListDecoderTest : FunSpec({

  test("List<String> decoded from yaml") {
    data class Test(val strings: List<String>)

    val config = ConfigLoader().loadConfigOrThrow<Test>("/test_list.yml")
    config shouldBe Test(list("test1", "test2", "test3"))
  }

})
