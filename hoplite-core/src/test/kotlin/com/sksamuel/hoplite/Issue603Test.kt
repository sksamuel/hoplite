package com.sksamuel.hoplite

import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

data class Issue603Worker(val batchSize: Int)
data class Issue603Application(val workers: Map<String, Issue603Worker>)
data class Issue603Config(val application: Issue603Application)

data class Issue603LinkedApplication(val workers: LinkedHashMap<String, Issue603Worker>)
data class Issue603LinkedConfig(val application: Issue603LinkedApplication)

data class Issue603NestedApplication(val groups: Map<String, Map<String, Int>>)
data class Issue603NestedConfig(val application: Issue603NestedApplication)

class Issue603Test : FunSpec({

  // gh-603: an env var overriding one entry of a file-defined map made the cascaded map node take
  // the env var's sourceKey and "_" delimiter. Denormalizing the map keys strips the parent's
  // "<sourceKey><delimiter>" prefix from each child's sourceKey, so siblings that only came from
  // the file (with a "." delimited sourceKey) were left with their full dotted path as their key.
  fun props() = PropertySource.string(
    """
    application.workers.alphaWorker.batchSize=10
    application.workers.betaWorker.batchSize=20
    """.trimIndent(),
    "props",
  )

  fun env(vararg vars: Pair<String, String>) =
    EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf(*vars) })

  test("overriding one map entry via env var does not change the keys of untouched siblings") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234"))
      .addPropertySource(props())
      .build()
      .loadConfigOrThrow<Issue603Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("overriding the other map entry via env var does not change the keys of untouched siblings") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_betaWorker_BATCHSIZE" to "1234"))
      .addPropertySource(props())
      .build()
      .loadConfigOrThrow<Issue603Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Issue603Worker(10),
      "betaWorker" to Issue603Worker(1234),
    )
  }

  test("an upper-case env var overriding a map entry keeps the file's spelling of the key") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_ALPHAWORKER_BATCHSIZE" to "1234"))
      .addPropertySource(props())
      .build()
      .loadConfigOrThrow<Issue603Config>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("LinkedHashMap keys of untouched siblings are not changed by an env var override") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_alphaWorker_BATCHSIZE" to "1234"))
      .addPropertySource(props())
      .build()
      .loadConfigOrThrow<Issue603LinkedConfig>()

    config.application.workers shouldBe linkedMapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("nested map keys from the file are not changed when an env var adds a key to the same nested map") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_GROUPS_first_newKey" to "3"))
      .addPropertySource(
        PropertySource.string(
          """
          application.groups.first.oldKey=1
          application.groups.second.otherKey=2
          """.trimIndent(),
          "props",
        )
      )
      .build()
      .loadConfigOrThrow<Issue603NestedConfig>()

    config.application.groups shouldBe mapOf(
      "first" to mapOf("oldKey" to 1, "newKey" to 3),
      "second" to mapOf("otherKey" to 2),
    )
  }
})
