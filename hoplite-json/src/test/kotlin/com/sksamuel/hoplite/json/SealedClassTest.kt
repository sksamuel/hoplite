package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Secret
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SealedClassTest : FunSpec({

  test("override sealed class attribute with env var") {
    withEnvironment("database_pass", "letmein") {
      val expected = Database.Mysql("foo.local", 1234, "sammy", Secret("letmein"))
      val actual = ConfigLoader()
        .loadConfigOrThrow<Config>("/sealed_test_1.json").database
      actual shouldBe expected
    }
  }

  test("should support objects with empty node") {
    val actual = ConfigLoader().loadConfigOrThrow<Config>("/sealed_test_2.json").database
    actual shouldBe Database.Embedded
  }

  test("should support objects with string name") {
    val actual = ConfigLoader().loadConfigOrThrow<Config>("/sealed_test_3.json").database
    actual shouldBe Database.Embedded
  }

  test("multiple objects should error") {
    shouldThrowAny {
      ConfigLoader().loadConfigOrThrow<Config2>("/sealed_test_2.json").database
    }.message.shouldContain("Cannot disambiguate between sealed class implementations:")
  }

  test("no objects should error") {
    shouldThrowAny {
      ConfigLoader().loadConfigOrThrow<Config3>("/sealed_test_2.json").database
    }.message.shouldContain("Sealed class class com.sksamuel.hoplite.json.Database3 does not define an object instance")
  }
})

data class Config(val database: Database)
sealed class Database {
  data class Mysql(val host: String, val port: Int, val user: String, val pass: Secret) : Database()
  data class Elastic(val host: String, val port: Int, val clusterName: String) : Database()
  object Embedded : Database()
}

data class Config2(val database: Database2)
sealed class Database2 {
  object Embedded1 : Database2()
  object Embedded2 : Database2()
}

data class Config3(val database: Database3)
sealed class Database3 {
  data class Mysql(val host: String, val port: Int, val user: String, val pass: Secret) : Database3()
  data class Elastic(val host: String, val port: Int, val clusterName: String) : Database3()
}
