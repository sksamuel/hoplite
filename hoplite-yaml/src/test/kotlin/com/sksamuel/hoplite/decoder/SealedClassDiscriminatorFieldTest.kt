package com.sksamuel.hoplite.decoder

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

sealed class Foo

@OptIn(ExperimentalHoplite::class)
class SealedClassDiscriminatorFieldTest : FunSpec({

  test("empty sealed type should error") {
    data class TestConfig(val database: Foo)
    shouldThrowAny {
      ConfigLoaderBuilder.create()
        .withExplicitSealedTypes()
        .addPropertySource(
          YamlPropertySource(
            """
database:
  _type: Elasticsearch
  host: localhost
  port: 9200
  index: foo
  """
          )
        ).build()
        .loadConfigOrThrow<TestConfig>()
    }.message.shouldContain("Sealed class `com.sksamuel.hoplite.decoder.Foo` does not define any subclasses")
  }

  test("sealed classes decoding using discriminator field for class subtype") {
    data class TestConfig(val database: Database)

    val config = ConfigLoaderBuilder.create()
      .withExplicitSealedTypes()
      .addPropertySource(
        YamlPropertySource(
          """
database:
  _type: Elasticsearch
  host: localhost
  port: 9200
  index: foo
  """
        )
      ).build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(database = Database.Elasticsearch("localhost", 9200, "foo"))
  }

  test("sealed classes decoding using discriminator field for object subtype") {
    data class TestConfig(val database: Database)

    val config = ConfigLoaderBuilder.create()
      .withExplicitSealedTypes()
      .addPropertySource(
        YamlPropertySource(
          """
database:
  _type: InMemory
"""
        )
      ).build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(database = Database.InMemory)
  }

  test("sealed classes decoding using top level field for object subtype") {
    data class TestConfig(val database: Database)

    val config = ConfigLoaderBuilder.create()
      .withExplicitSealedTypes()
      .addPropertySource(
        YamlPropertySource(
          """
database: InMemory
"""
        )
      ).build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(database = Database.InMemory)
  }

  test("sealed classes without discriminator field should error") {
    data class TestConfig(val database: Database)

    shouldThrowAny {
      ConfigLoaderBuilder.create()
        .withExplicitSealedTypes()
        .addPropertySource(
          YamlPropertySource(
            """
database:
  host: foo
  """
          )
        ).build()
        .loadConfigOrThrow<TestConfig>()
    }.message.shouldContain("Invalid discriminator field to select sealed subtype. Must specify `_type` to be a valid subtype of `com.sksamuel.hoplite.decoder.Database`.")
  }

  test("sealed classes with integer discriminator field should error") {
    data class TestConfig(val database: Database)

    shouldThrowAny {
      ConfigLoaderBuilder.create()
        .withExplicitSealedTypes()
        .addPropertySource(
          YamlPropertySource(
            """
database:
  _type: 123
  host: localhost
  port: 9200
  index: foo
  """
          )
        ).build()
        .loadConfigOrThrow<TestConfig>()
    }.message.shouldContain("No sealed subtype of `com.sksamuel.hoplite.decoder.Database` was found using the discriminator value `123`")
  }

  test("sealed classes with unknown discriminator field should error") {
    data class TestConfig(val database: Database)

    shouldThrowAny {
      ConfigLoaderBuilder.create()
        .withExplicitSealedTypes()
        .addPropertySource(
          YamlPropertySource(
            """
database:
  _type: foo
  host: localhost
  port: 9200
  index: foo
  """
          )
        ).build()
        .loadConfigOrThrow<TestConfig>()
    }.message.shouldContain("No sealed subtype of `com.sksamuel.hoplite.decoder.Database` was found using the discriminator value `foo`")
  }

  test("list of sealed classes using discriminator field per entry") {
    data class TestConfig(val databases: List<Database>)

    val config = ConfigLoaderBuilder.create()
      .withExplicitSealedTypes()
      .addPropertySource(
        YamlPropertySource(
          """
databases:
  - _type: Elasticsearch
    host: localhost
    port: 9200
    index: foo
  - _type: Elasticsearch
    host: localhost
    port: 9300
    index: bar
  - _type: Postgres
    host: localhost
    port: 5234
    schema: public
    table: faz
"""
        )
      ).build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(
      listOf(
        Database.Elasticsearch("localhost", 9200, "foo"),
        Database.Elasticsearch("localhost", 9300, "bar"),
        Database.Postgres("localhost", 5234, "public", "faz")
      )
    )
  }

  test("list of sealed classes with missing discriminator field in a single entry should error") {
    data class TestConfig(val databases: List<Database>)

    shouldThrowAny {
      ConfigLoaderBuilder.create()
        .withExplicitSealedTypes()
        .addPropertySource(
          YamlPropertySource(
            """
databases:
  - _type: Elasticsearch
    host: localhost
    port: 9200
    index: foo
  - host: localhost
    port: 9300
    index: bar
  - _type: Postgres
    host: localhost
    port: 5234
    schema: public
    table: faz
"""
          )
        ).build()
        .loadConfigOrThrow<TestConfig>()
    }.message.shouldContain("Invalid discriminator field to select sealed subtype. Must specify `_type` to be a valid subtype of `com.sksamuel.hoplite.decoder.Database`.")
  }

  test("list of sealed classes mixing class and object implementations") {
    data class TestConfig(val databases: List<Database>)

    val config = ConfigLoaderBuilder.create()
      .withExplicitSealedTypes()
      .addPropertySource(
        YamlPropertySource(
          """
databases:
  - _type: Elasticsearch
    host: localhost
    port: 9200
    index: foo
  - _type: InMemory
  - _type: Postgres
    host: localhost
    port: 5234
    schema: public
    table: faz
"""
        )
      ).build()
      .loadConfigOrThrow<TestConfig>()

    config shouldBe TestConfig(
      listOf(
        Database.Elasticsearch("localhost", 9200, "foo"),
        Database.InMemory,
        Database.Postgres("localhost", 5234, "public", "faz")
      )
    )
  }

})
