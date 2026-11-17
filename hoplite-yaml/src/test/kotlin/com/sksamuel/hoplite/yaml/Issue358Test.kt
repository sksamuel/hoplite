package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// gh-358: a higher-precedence source can set one element of a list from a YAML file by its index
class Issue358Test : FunSpec({

  data class Db(val host: String, val port: Int)
  data class Cfg(val databases: List<Db>)

  fun builder(env: Map<String, String> = emptyMap(), args: List<String> = emptyList(), lookup: Boolean = true) =
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.commandLine(args.toTypedArray()))
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))
      .addResourceSource("/issue358.yml")
      .withAmbientSourceLookup(lookup)

  test("an env var sets a field of one element of a YAML list") {
    listOf(true, false).forEach { lookup ->
      builder(env = mapOf("DATABASES_1_PORT" to "9301"), lookup = lookup).build().loadConfigOrThrow<Cfg>() shouldBe
        Cfg(listOf(Db("a", 1), Db("b", 9301)))
    }
  }

  test("a command line argument sets a field of one element of a YAML list, and an index past the end appends") {
    builder(args = listOf("--databases.0.host=z", "--databases.2.host=c", "--databases.2.port=3"))
      .build().loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf(Db("z", 1), Db("b", 2), Db("c", 3)))
  }

  test("strict mode sees the merged elements as used") {
    listOf(true, false).forEach { lookup ->
      builder(env = mapOf("DATABASES_1_PORT" to "9301"), args = listOf("--databases.2.host=c", "--databases.2.port=3"), lookup = lookup)
        .strict().build().loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf(Db("a", 1), Db("b", 9301), Db("c", 3)))
    }
  }

  test("a YAML list from a higher-precedence source replaces a lower YAML list") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.string("databases:\n  - host: x\n    port: 9\n", "yml"))
      .addResourceSource("/issue358.yml")
      .build().loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf(Db("x", 9)))
  }

  test("strict mode with a list given only by index") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.commandLine(arrayOf("--databases.1.host=b", "--databases.1.port=2", "--databases.0.host=a", "--databases.0.port=1")))
      .strict().build().loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf(Db("a", 1), Db("b", 2)))
  }
})
