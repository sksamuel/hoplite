package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Masked
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

class SealedClassTest : FunSpec({

  test("override sealed class attribute with env var") {
    withEnvironment("database__pass", "letmein") {
      val expected = Database.Mysql("foo.local", 1234, "sammy", Masked("letmein"))
      val actual = ConfigLoader().loadConfigOrThrow<Config>("/sealed_test_1.json").database
      actual shouldBe expected
    }
  }

})

data class Config(val database: Database)

sealed class Database {
  data class Mysql(val host: String, val port: Int, val user: String, val pass: Masked) : Database()
  data class Elastic(val host: String, val port: Int, val clusterName: String) : Database()
}
