package com.sksamuel.hoplite

import io.kotlintest.should
import io.kotlintest.specs.StringSpec
import io.kotlintest.specs.WordSpec

class TReaderTest : WordSpec({
  "Derived reader for T" should {
    "return error for non data classes" {
      Reader.forT<Thread>()
    }
  }
})