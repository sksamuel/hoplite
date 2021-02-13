package com.sksamuel.hoplite.toml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

data class Database2(val server: String)
data class Owner2(val name: String)

data class Unused(val title: String,
                  val owner: Owner2,
                  val database: Database2)

class UnusedConfigValueTest : FunSpec() {
  init {
    test("unused values should not throw in lenient mode") {
      ConfigLoader().loadConfigOrThrow<Unused>("/unused.toml")
    }
    test("unused values should throw in strict mode") {
      val error = shouldThrowAny {
        ConfigLoader.Builder().strict().build().loadConfigOrThrow<Unused>("/unused.toml")
      }
      error.message.shouldContain("'owner': Config values were not used: dob")
      error.message.shouldContain("'database': Config values were not used: connection_max, enabled")
    }
  }
}
