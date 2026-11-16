package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.internal.CascadeMode
import com.sksamuel.hoplite.sources.EnvironmentVariablesLookupPropertySource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesLookupPropertySource
import com.sksamuel.hoplite.sources.SystemPropertiesPropertySource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf

@OptIn(ExperimentalHoplite::class)
// gh-606: unprefixed env vars and sysprops with an empty prefix are looked up per bind instead of imported
class LookupPropertySourceTest : FunSpec({

  data class Pool(val maxPoolSize: Int, val minIdle: Int = 1)
  data class Db(val url: String, val pool: Pool)
  data class DbCfg(val db: Db)
  data class Host(val host: String, val port: Int = 80)

  val junk = mapOf(
    "PATH" to "/usr/bin:/bin",
    "PS1" to "\${debian_chroot:+(\$debian_chroot)}\\u@\\h:\\w\\$ ",
    "BASH_FUNC_ml%%" to "() {  eval \"\$(\${LMOD_DIR}/ml_cmd \"\$@\")\" }",
    "_" to "/usr/bin/env",
  )

  fun builder(env: Map<String, String>) = ConfigLoaderBuilder.defaultWithoutPropertySources()
    .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { junk + env }))
    .withAmbientSourceLookup()

  fun loader(env: Map<String, String>) = builder(env).build()

  test("the switch replaces the unprefixed env source with a lookup source, and leaves prefixed sources alone") {
    val sources = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource())
      .addPropertySource(EnvironmentVariablesPropertySource(prefix = "APP_"))
      .addPropertySource(SystemPropertiesPropertySource(prefix = ""))
      .addPropertySource(SystemPropertiesPropertySource)
      .withAmbientSourceLookup()
      .build()
      .propertySources
    sources[0].shouldBeInstanceOf<EnvironmentVariablesLookupPropertySource>()
    sources[1].shouldBeInstanceOf<EnvironmentVariablesPropertySource>()
    sources[2].shouldBeInstanceOf<SystemPropertiesLookupPropertySource>()
    sources[3] shouldBe SystemPropertiesPropertySource
  }

  test("the switch is on by default") {
    ConfigLoaderBuilder.default().build().propertySources[0]
      .shouldBeInstanceOf<EnvironmentVariablesLookupPropertySource>()
    ConfigLoaderBuilder.defaultWithoutPropertySources().addEnvironmentSource().build().propertySources[0]
      .shouldBeInstanceOf<EnvironmentVariablesLookupPropertySource>()
  }

  test("the switch opts out to the tree-based sources") {
    ConfigLoaderBuilder.default().withAmbientSourceLookup(false).build().propertySources
      .none { it is LookupPropertySource } shouldBe true
    data class Cfg(val maxPoolSize: Int = 1)
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("MAX_POOL_SIZE" to "5") }))
      .withAmbientSourceLookup(false)
      .build()
      .loadNodeOrThrow().atPath("max.pool.size").shouldBeInstanceOf<StringNode>().value shouldBe "5"
  }

  test("names as the tree-based source spells them still bind") {
    loader(mapOf("DB_URL" to "jdbc:x", "DB_POOL_MAXPOOLSIZE" to "10"))
      .loadConfigOrThrow<DbCfg>() shouldBe DbCfg(Db("jdbc:x", Pool(10)))
  }

  test("each path has one env var name: an underscore separates path elements, never the words of a name") {
    EnvironmentVariablesLookupPropertySource.envName(listOf("db", "pool", "maxPoolSize")) shouldBe "DB_POOL_MAXPOOLSIZE"
    EnvironmentVariablesLookupPropertySource.envName(listOf("db", "max-pool-size")) shouldBe "DB_MAXPOOLSIZE"
    EnvironmentVariablesLookupPropertySource.envName(listOf("legacy_timeout")) shouldBe "LEGACYTIMEOUT"
    // DB_POOL_MAX_POOL_SIZE is db.pool.max.pool.size, as in Spring's relaxed binding, so maxPoolSize is missing
    data class Max(val pool: Map<String, String>)
    loader(mapOf("DB_POOL_MAX_POOL_SIZE" to "10")).configBinder()
      .bindOrThrow<Max>("db.pool.max") shouldBe Max(mapOf("SIZE" to "10"))
    shouldThrow<ConfigException> {
      loader(mapOf("DB_URL" to "jdbc:x", "DB_POOL_MAX_POOL_SIZE" to "10")).loadConfigOrThrow<DbCfg>()
    }.message shouldContain "Pool"
  }

  test("env vars are matched ignoring case") {
    loader(mapOf("db_url" to "jdbc:x", "Db_Pool_MaxPoolSize" to "10"))
      .loadConfigOrThrow<DbCfg>() shouldBe DbCfg(Db("jdbc:x", Pool(10)))
  }

  test("an alias is spelled as a field name is") {
    data class Cfg(@ConfigAlias("legacy_timeout") val timeout: Int)
    loader(mapOf("LEGACYTIMEOUT" to "5")).loadConfigOrThrow<Cfg>() shouldBe Cfg(5)
  }

  test("junk env vars don't fail the load, in lenient or strict mode") {
    data class Cfg(val name: String)
    loader(mapOf("NAME" to "x")).loadConfigOrThrow<Cfg>() shouldBe Cfg("x")
    builder(mapOf("NAME" to "x")).strict().build().loadConfigOrThrow<Cfg>() shouldBe Cfg("x")
  }

  test("an unresolved substitution in a value that is looked up is still reported") {
    data class Cfg(val name: String)
    shouldThrow<ConfigException> {
      loader(mapOf("NAME" to "\${NOPE_606}")).loadConfigOrThrow<Cfg>()
    }.message shouldContain "Unresolved substitution \${NOPE_606}"
  }

  test("preprocessors run on looked up values") {
    data class Cfg(val name: String, val other: String)
    loader(mapOf("NAME" to "abc", "OTHER" to "\${name}-def")).loadConfigOrThrow<Cfg>() shouldBe Cfg("abc", "abc-def")
  }

  test("map keys keep the underscores and case of the env var") {
    data class Cfg(val labels: Map<String, String>)
    loader(mapOf("LABELS_MY_KEY" to "a", "LABELS_ENV" to "b", "LABELS_my_lower" to "c"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(mapOf("MY_KEY" to "a", "ENV" to "b", "my_lower" to "c"))
  }

  test("map keys differing only in case stay apart") {
    data class Cfg(val labels: Map<String, String>)
    loader(mapOf("LABELS_a" to "1", "LABELS_A" to "2"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(mapOf("a" to "1", "A" to "2"))
  }

  test("fields differing only in case read the env var spelled as the field") {
    data class Cfg(val someSetting: String, val somesetting: String, val SOMESETTING: String)
    loader(mapOf("SOMESETTING" to "c", "somesetting" to "b", "someSetting" to "a"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg("a", "b", "c")
  }

  test("dashes in env var names are ignored, as the path normalizer does") {
    data class Cfg(val maxPoolSize: Int)
    loader(mapOf("max-pool-size" to "4")).loadConfigOrThrow<Cfg>() shouldBe Cfg(4)
  }

  test("each env var goes to one entry of a map of maps, under the shortest key that reads it") {
    data class Cfg(val apps: Map<String, Map<String, Host>>)
    loader(mapOf("APPS_ROOT_alpha_HOST" to "a", "APPS_ROOT_alpha_PORT" to "1", "APPS_MY_APP_beta_HOST" to "b"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(
      mapOf(
        "ROOT" to mapOf("alpha" to Host("a", 1)),
        "MY" to mapOf("APP_beta" to Host("b")),
      )
    )
  }

  test("map keys with underscores whose values are data classes") {
    data class Cfg(val servers: Map<String, Host>)
    loader(mapOf("SERVERS_MY_SERVER_HOST" to "a", "SERVERS_MY_SERVER_PORT" to "1", "SERVERS_OTHER_HOST" to "b"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(mapOf("MY_SERVER" to Host("a", 1), "OTHER" to Host("b")))
  }

  test("lists by index, in index order, skipping missing indices") {
    data class Cfg(val hosts: List<String>, val dbs: List<Host>)
    loader(
      mapOf(
        "HOSTS_10" to "c", "HOSTS_2" to "b", "HOSTS_0" to "a",
        "DBS_0_HOST" to "x", "DBS_1_HOST" to "y", "DBS_1_PORT" to "2",
      )
    ).loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf("a", "b", "c"), listOf(Host("x"), Host("y", 2)))
  }

  test("an env var for one list index sets that element of a file's list (gh-358)") {
    data class Db(val host: String = "default", val port: Int)
    data class Cfg(val databases: List<Db>)
    builder(mapOf("DATABASES_1_PORT" to "9301"))
      .addMapSource(mapOf("databases" to listOf(mapOf("host" to "a", "port" to 1), mapOf("host" to "b", "port" to 2))))
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf(Db("a", 1), Db("b", 9301)))
  }

  test("a map whose keys look like indices stays a map") {
    data class Cfg(val codes: Map<String, String>)
    loader(mapOf("CODES_0" to "a", "CODES_10" to "b"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(mapOf("0" to "a", "10" to "b"))
  }

  test("a list given as one comma separated value") {
    data class Cfg(val hosts: List<String>, val ports: Set<Int>)
    loader(mapOf("HOSTS" to "a,b", "PORTS" to "1,2")).loadConfigOrThrow<Cfg>() shouldBe Cfg(listOf("a", "b"), setOf(1, 2))
  }

  test("sealed types are inferred from the looked up values") {
    data class Cfg(val db: Database)
    loader(mapOf("DB_HOST" to "h", "DB_PORT" to "5432"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Database.Postgres("h", 5432))
    loader(mapOf("DB_FILE" to "/tmp/x"))
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Database.Sqlite("/tmp/x"))
  }

  test("sealed types selected by name with explicit sealed types") {
    data class Cfg(val db: Database)
    builder(mapOf("DB" to "Sqlite", "DB_FILE" to "/tmp/x")).withExplicitSealedTypes().build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Database.Sqlite("/tmp/x"))
    builder(mapOf("DB" to "InMemory")).withExplicitSealedTypes().build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Database.InMemory)
  }

  test("an env var switches the sealed subtype a file selects") {
    data class Cfg(val db: Database)
    builder(mapOf("DB" to "Sqlite", "DB_FILE" to "/tmp/x"))
      .addMapSource(mapOf("db" to mapOf("_type" to "Postgres", "host" to "h", "port" to "1")))
      .withExplicitSealedTypes()
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(Database.Sqlite("/tmp/x"))
  }

  test("a decoder the binder can't see into gets everything under its path") {
    data class Cfg(val props: java.util.Properties)
    val env = mapOf("PROPS_X" to "y", "PROPS_Y" to "z")
    val cfg = loader(env).loadConfigOrThrow<Cfg>()
    cfg.props.size shouldBe 2
    // the same as the tree-based source gives
    cfg shouldBe ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { env }))
      .build()
      .loadConfigOrThrow<Cfg>()
  }

  test("an env var registered before a file takes precedence over it") {
    builder(mapOf("DB_POOL_MAXPOOLSIZE" to "10"))
      .addMapSource(mapOf("db" to mapOf("url" to "jdbc:file", "pool" to mapOf("max-pool-size" to "5", "min-idle" to 2))))
      .build()
      .loadConfigOrThrow<DbCfg>() shouldBe DbCfg(Db("jdbc:file", Pool(10, 2)))
  }

  test("a file registered before an env var takes precedence over it") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("db" to mapOf("pool" to mapOf("maxPoolSize" to "5"))))
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = {
        mapOf("DB_URL" to "jdbc:env", "DB_POOL_MAXPOOLSIZE" to "10", "DB_POOL_MINIDLE" to "3")
      }))
      .addMapSource(mapOf("db" to mapOf("url" to "jdbc:file", "pool" to mapOf("minIdle" to "2"))))
      .withAmbientSourceLookup()
      .build()
      .loadConfigOrThrow<DbCfg>() shouldBe DbCfg(Db("jdbc:env", Pool(5, 3)))
  }

  test("CascadeMode.Error reports an env var overriding a file, but not the tree overriding itself") {
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addMapSource(mapOf("db" to mapOf("url" to "jdbc:file", "pool" to mapOf("maxPoolSize" to "5"))))
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf("OTHER" to "x") }))
      .withAmbientSourceLookup()
      .withCascadeMode(CascadeMode.Error)
      .build()
      .loadConfigOrThrow<DbCfg>() shouldBe DbCfg(Db("jdbc:file", Pool(5)))
    shouldThrow<ConfigException> {
      builder(mapOf("DB_URL" to "jdbc:env"))
        .addMapSource(mapOf("db" to mapOf("url" to "jdbc:file", "pool" to mapOf("maxPoolSize" to "5"))))
        .withCascadeMode(CascadeMode.Error)
        .build()
        .loadConfigOrThrow<DbCfg>()
    }.message shouldContain "Overridden"
  }

  test("a config binder binds several classes and prefixes from one load") {
    data class Server(val port: Int, val maxThreads: Int = 4)
    var reads = 0
    val env = mapOf("DB_URL" to "jdbc:x", "DB_POOL_MAXPOOLSIZE" to "10", "SERVER_PORT" to "8080", "SERVER_MAXTHREADS" to "8")
    val binder = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(EnvironmentVariablesPropertySource(environmentVariableMap = { reads++; env }))
      .addMapSource(mapOf("server" to mapOf("port" to "80")))
      .withAmbientSourceLookup()
      .build()
      .configBinder()
    binder.bindOrThrow<Db>("db") shouldBe Db("jdbc:x", Pool(10))
    binder.bindOrThrow<Server>("server") shouldBe Server(8080, 8)
    binder.bindOrThrow<Pool>("db.pool") shouldBe Pool(10)
    // the env vars are read once, when the binder is created
    reads shouldBe 1
  }

  test("preprocessors that handle one value at a time run once per value across the binds of a config binder") {
    data class Server(val port: Int, val secret: String)
    data class Db(val url: String, val secret: String)
    val calls = mutableListOf<String>()
    @Suppress("DEPRECATION")
    val fetch = object : com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor() {
      override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> =
        if (node is StringNode && node.value.startsWith("remote:")) {
          calls += node.value
          node.copy(value = node.value.removePrefix("remote:").uppercase()).valid()
        } else node.valid()
    }
    val binder = builder(mapOf("SERVER_PORT" to "8080", "DB_URL" to "jdbc:x", "DB_SECRET" to "remote:db", "APP_NAME" to "a"))
      .addMapSource(mapOf("server" to mapOf("port" to "80", "secret" to "remote:server"), "app" to mapOf("id" to "\${random.uuid}")))
      .addPreprocessor(fetch)
      .build()
      .configBinder()
    binder.bindOrThrow<Server>("server") shouldBe Server(8080, "SERVER")
    binder.bindOrThrow<Db>("db") shouldBe Db("jdbc:x", "DB")
    binder.bindOrThrow<Server>("server") shouldBe Server(8080, "SERVER")
    calls.sorted() shouldBe listOf("remote:db", "remote:server")
    // a random value is the same in every bind, as when the tree is preprocessed once
    data class Id(val id: String, val name: String)
    binder.bindOrThrow<Id>("app") shouldBe binder.bindOrThrow<Id>("app")
  }

  test("a looked up value can refer to a value from a file, and the other way around") {
    data class Cfg(val host: String, val url: String, val port: Int, val address: String)
    builder(mapOf("URL" to "http://\${host}:\${port}", "PORT" to "8080"))
      .addMapSource(mapOf("host" to "example.com", "address" to "\${host}:\${port}"))
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg("example.com", "http://example.com:8080", 8080, "example.com:8080")
  }

  test("a prefix is spelled as a field name is") {
    data class Cfg(val maxSize: Int)
    loader(mapOf("MYPOOL_MAXSIZE" to "3")).configBinder().bindOrThrow<Cfg>("myPool") shouldBe Cfg(3)
    loader(mapOf("MY_POOL_MAXSIZE" to "3")).configBinder().bindOrThrow<Cfg>("my.pool") shouldBe Cfg(3)
  }

  test("a class with only defaults binds when nothing is set") {
    data class Cfg(val name: String = "default")
    loader(emptyMap()).loadConfigOrThrow<Cfg>() shouldBe Cfg()
  }

  test("a missing value is reported as missing") {
    data class Cfg(val name: String)
    shouldThrow<ConfigException> { loader(emptyMap()).loadConfigOrThrow<Cfg>() }.message shouldContain "name"
  }

  test("loadNode imports the env vars in full") {
    val node = loader(mapOf("DB_URL" to "jdbc:x", "MAX_POOL_SIZE" to "10")).loadNodeOrThrow()
    node.atPath("db.url").shouldBeInstanceOf<StringNode>().value shouldBe "jdbc:x"
    node.atPath("max.pool.size").shouldBeInstanceOf<StringNode>().value shouldBe "10"
    node.atPath("path").shouldBeInstanceOf<StringNode>().value shouldBe "/usr/bin:/bin"
  }

  test("the report shows the looked up values and the env var each came from") {
    val output = StringBuilder()
    builder(mapOf("DB_URL" to "jdbc:x", "DB_POOL_MAXPOOLSIZE" to "10"))
      .withReport()
      .withReportPrintFn { output.append(it) }
      .build()
      .loadConfigOrThrow<DbCfg>()
    val report = output.toString()
    report shouldContain "Env Var (lookup)"
    report shouldContain "DB_POOL_MAXPOOLSIZE"
    report shouldContain "DB_URL"
    report shouldNotContain "BASH_FUNC"
    report shouldNotContain "PATH"
  }

  test("system properties with an empty prefix are looked up, and snapshotted at the first bind") {
    data class Cfg(val maxPoolSize: Int, val name: String)
    try {
      System.setProperty("app606.max-pool-size", "7")
      System.setProperty("app606.name", "a")
      val binder = ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(SystemPropertiesPropertySource(prefix = ""))
        .withAmbientSourceLookup()
        .build()
        .configBinder()
      // nothing is read until the first bind, which snapshots the properties for every later bind
      System.setProperty("app606.name", "b")
      binder.bindOrThrow<Cfg>("app606") shouldBe Cfg(7, "b")
      System.setProperty("app606.name", "c")
      binder.bindOrThrow<Cfg>("app606") shouldBe Cfg(7, "b")
    } finally {
      System.clearProperty("app606.max-pool-size")
      System.clearProperty("app606.name")
    }
  }

  test("system property map keys with dots") {
    data class Cfg(val labels: Map<String, String>)
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(SystemPropertiesLookupPropertySource { mapOf("labels.a.b" to "1", "labels.c" to "2") })
      .build()
      .loadConfigOrThrow<Cfg>() shouldBe Cfg(mapOf("a.b" to "1", "c" to "2"))
  }
}) {
  sealed interface Database {
    data class Postgres(val host: String, val port: Int) : Database
    data class Sqlite(val file: String) : Database
    data object InMemory : Database
  }
}
