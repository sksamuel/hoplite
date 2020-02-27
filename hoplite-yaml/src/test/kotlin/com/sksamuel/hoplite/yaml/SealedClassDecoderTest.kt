package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

sealed class Database {
  data class Elasticsearch(val host: String, val port: Int, val index: String) : Database()
  data class Postgres(val host: String, val port: Int, val schema: String, val table: String) : Database()
}

sealed class Lonely

class SealedClassDecoderTest : FunSpec({

  test("sealed classes decoding") {
    data class TestConfig(val database: Database)

    val config = ConfigLoader().loadConfigOrThrow<TestConfig>("/sealed_class.yml")
    config shouldBe TestConfig(database = Database.Elasticsearch("localhost", 9200, "foo"))
  }

  test("list of sealed classes") {
    data class TestConfig(val databases: List<Database>)

    val config = ConfigLoader().loadConfigOrThrow<TestConfig>("/sealed_class_list.yml")
    config shouldBe TestConfig(
      listOf(
        Database.Elasticsearch("localhost", 9200, "foo"),
        Database.Elasticsearch("localhost", 9300, "bar"),
        Database.Postgres("localhost", 5234, "public", "faz")
      )
    )
  }

  test("should error for sealed without impls") {
    data class TestConfig(val lonely: Lonely)
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<TestConfig>("/sealed_class_list.yml")
    }.message shouldBe "Error loading config because:\n" +
      "\n" +
      "    - Could not instantiate 'com.sksamuel.hoplite.yaml.`SealedClassDecoderTest\$1\$3\$TestConfig`' because:\n" +
      "    \n" +
      "        - 'lonely': Sealed class class com.sksamuel.hoplite.yaml.Lonely does not define any subclasses"
  }
})
