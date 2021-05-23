package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@JvmInline
value class ValueType(val value: String)
data class ValueConfig(val foo: ValueType)

class ValueTypeTest : FunSpec() {
  init {
    test("should decode value types") {
      ConfigLoader().loadConfigOrThrow<ValueConfig>("/value.toml").foo shouldBe ValueType("bar")
    }
  }
}
