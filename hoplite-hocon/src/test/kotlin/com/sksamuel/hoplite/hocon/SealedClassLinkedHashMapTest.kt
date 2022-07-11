package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SealedClassLinkedHashMapTest : FunSpec() {
  init {
    test("linked map of sealed interface should preserve order from hocon") {
      ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.resource("/sealed_class_linked_hash_map.conf"))
        .build()
        .loadConfigOrThrow<Configuration>()
        .expressions shouldBe linkedMapOf(
        "const1" to Const(number = 2),
        "const2" to Const(number = 10),
        "text1" to Text(str = "This is a example text."),
        "const3" to Const(number = 42)
      )
    }
  }
}

data class Configuration(val expressions: LinkedHashMap<String, Expr>? = null)

sealed interface Expr

data class Const(val number: Int) : Expr
data class Text(val str: String) : Expr
