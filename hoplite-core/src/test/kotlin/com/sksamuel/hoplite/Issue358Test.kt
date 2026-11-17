package com.sksamuel.hoplite

import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalHoplite::class)
// gh-358: a higher-precedence source can set one element of a list by its index
class Issue358Test : FunSpec({

  data class Db(val host: String = "default", val port: Int)
  data class Cfg(val databases: List<Db>, val labels: Map<String, String> = emptyMap())

  val base = mapOf(
    "databases" to listOf(mapOf("host" to "a", "port" to 1), mapOf("host" to "b", "port" to 2)),
    "labels" to mapOf("x" to "1"),
  )

  fun builder(
    env: Map<String, String> = emptyMap(),
    args: List<String> = emptyList(),
    map: Map<String, Any> = emptyMap(),
    lookup: Boolean = true,
  ) = ConfigLoaderBuilder.defaultWithoutPropertySources()
    .addPropertySource(PropertySource.commandLine(args.toTypedArray()))
    .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))
    .apply { if (map.isNotEmpty()) addMapSource(map) }
    .addMapSource(base)
    .withAmbientSourceLookup(lookup)

  fun load(
    env: Map<String, String> = emptyMap(),
    args: List<String> = emptyList(),
    map: Map<String, Any> = emptyMap(),
    lookup: Boolean = true,
  ) = builder(env, args, map, lookup).build().loadConfigOrThrow<Cfg>()

  val merged = listOf(Db("a", 1), Db("b", 9301))

  test("an env var sets a field of one list element, looked up") {
    load(env = mapOf("DATABASES_1_PORT" to "9301")).databases shouldBe merged
  }

  test("an env var sets a field of one list element, with the tree-based env source") {
    load(env = mapOf("DATABASES_1_PORT" to "9301"), lookup = false).databases shouldBe merged
  }

  test("a command line argument sets a field of one list element") {
    load(args = listOf("--databases.1.port=9301")).databases shouldBe merged
  }

  test("a map source sets a field of one list element") {
    load(map = mapOf("databases.1.port" to "9301")).databases shouldBe merged
  }

  test("indices from several sources merge into the same list") {
    listOf(true, false).forEach { lookup ->
      load(
        env = mapOf("DATABASES_1_PORT" to "9301", "DATABASES_0_HOST" to "envhost"),
        args = listOf("--databases.0.host=clihost"),
        lookup = lookup,
      ).databases shouldBe listOf(Db("clihost", 1), Db("b", 9301))
    }
  }

  test("an index past the end of the list appends an element") {
    listOf(true, false).forEach { lookup ->
      load(env = mapOf("DATABASES_2_HOST" to "c", "DATABASES_2_PORT" to "3"), lookup = lookup)
        .databases shouldBe listOf(Db("a", 1), Db("b", 2), Db("c", 3))
    }
    load(args = listOf("--databases.2.host=c", "--databases.2.port=3"))
      .databases shouldBe listOf(Db("a", 1), Db("b", 2), Db("c", 3))
  }

  test("indices past the end are appended in index order, closing up any gap") {
    listOf(true, false).forEach { lookup ->
      load(
        env = mapOf("DATABASES_9_HOST" to "e", "DATABASES_9_PORT" to "5", "DATABASES_5_HOST" to "d", "DATABASES_5_PORT" to "4"),
        lookup = lookup,
      ).databases shouldBe listOf(Db("a", 1), Db("b", 2), Db("d", 4), Db("e", 5))
    }
  }

  test("an appended element must be complete, as no lower element fills it in") {
    shouldThrow<ConfigException> {
      load(args = listOf("--databases.5.host=c"))
    }.message shouldContain "port"
  }

  test("a whole list from a higher-precedence source replaces the lower list") {
    load(map = mapOf("databases" to listOf(mapOf("host" to "z", "port" to 9))))
      .databases shouldBe listOf(Db("z", 9))
  }

  test("a whole list from a higher-precedence source replaces lower indices") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(base)
      .addPropertySource(PropertySource.commandLine(arrayOf("--databases.1.port=9301")))
      .build()
      .loadConfigOrThrow<Cfg>()
      .databases shouldBe listOf(Db("a", 1), Db("b", 2))
  }

  test("a list given only by index") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.commandLine(arrayOf("--databases.1.host=b", "--databases.0.host=a", "--databases.0.port=1", "--databases.1.port=2")))
      .build()
      .loadConfigOrThrow<Cfg>()
      .databases shouldBe listOf(Db("a", 1), Db("b", 2))
  }

  test("map entries still merge") {
    val expected = mapOf("x" to "1", "y" to "2")
    // a key only an env var gives is spelled as the env var spells it
    load(env = mapOf("LABELS_Y" to "2")).labels.mapKeys { it.key.lowercase() } shouldBe expected
    load(env = mapOf("LABELS_Y" to "2"), lookup = false).labels.mapKeys { it.key.lowercase() } shouldBe expected
    load(args = listOf("--labels.y=2")).labels shouldBe expected
    load(map = mapOf("labels.y" to "2")).labels shouldBe expected
  }

  test("a map with keys that look like indices stays a map") {
    data class Codes(val codes: Map<String, String>)
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(PropertySource.commandLine(arrayOf("--codes.1=b")))
      .addMapSource(mapOf("codes" to mapOf("0" to "a")))
      .build()
      .loadConfigOrThrow<Codes>() shouldBe Codes(mapOf("0" to "a", "1" to "b"))
  }

  test("CascadeMode.Error reports an element's field set by index") {
    listOf(true, false).forEach { lookup ->
      shouldThrow<ConfigException> {
        builder(env = mapOf("DATABASES_1_PORT" to "9301"), lookup = lookup)
          .withCascadeMode(CascadeMode.Error)
          .build()
          .loadConfigOrThrow<Cfg>()
      }.message shouldContain "databases"
    }
  }

  test("CascadeMode.Error allows appending an element by index") {
    builder(args = listOf("--databases.2.host=c", "--databases.2.port=3"))
      .withCascadeMode(CascadeMode.Error)
      .build()
      .loadConfigOrThrow<Cfg>()
      .databases shouldBe listOf(Db("a", 1), Db("b", 2), Db("c", 3))
  }

  test("CascadeMode.Fallthrough takes the indices given in place of the lower list") {
    builder(args = listOf("--databases.1.port=9301"))
      .withCascadeMode(CascadeMode.Fallthrough)
      .build()
      .loadConfigOrThrow<Cfg>()
      .databases shouldBe listOf(Db(port = 9301))
  }

  test("an index sets an element of a list within a list element") {
    data class Node(val name: String, val tags: List<String>)
    data class Cluster(val nodes: List<Node>)
    listOf(true, false).forEach { lookup ->
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("NODES_0_TAGS_1" to "z") }))
        .addMapSource(mapOf("nodes" to listOf(mapOf("name" to "n0", "tags" to listOf("a", "b")), mapOf("name" to "n1", "tags" to listOf("c")))))
        .withAmbientSourceLookup(lookup)
        .build()
        .loadConfigOrThrow<Cluster>() shouldBe Cluster(listOf(Node("n0", listOf("a", "z")), Node("n1", listOf("c"))))
    }
  }

  test("a system property sets a field of one list element") {
    listOf(true, false).forEach { lookup ->
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(SystemPropertiesPropertySource(systemPropertiesMap = { mapOf("databases.1.port" to "9301") }, prefix = ""))
        .addPropertySource(SystemPropertiesPropertySource(systemPropertiesMap = { mapOf("config.override.databases.0.port" to "9300") }))
        .addMapSource(base)
        .withAmbientSourceLookup(lookup)
        .build()
        .loadConfigOrThrow<Cfg>()
        .databases shouldBe listOf(Db("a", 9300), Db("b", 9301))
    }
  }
})
