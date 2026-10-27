package com.sksamuel.hoplite

import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

data class MapKeySpellingApplication(val workers: Map<String, Issue603Worker>)
data class MapKeySpellingConfig(val application: MapKeySpellingApplication)

data class MapKeySpellingLinkedApplication(val workers: LinkedHashMap<String, Issue603Worker>)
data class MapKeySpellingLinkedConfig(val application: MapKeySpellingLinkedApplication)

data class MapKeySpellingIntApplication(val sizes: Map<String, Int>)
data class MapKeySpellingIntConfig(val application: MapKeySpellingIntApplication)

/**
 * When several sources define the same map entry, the key is spelled as in the lowest-precedence source that
 * defines it, while values still follow normal precedence. Map keys are identifiers that application code looks
 * up, so an override (typically an upper-case env var) shouldn't rename the entry it overrides.
 */
@OptIn(ExperimentalHoplite::class)
class MapKeySpellingTest : FunSpec({

  fun props(vararg lines: String) = PropertySource.string(lines.joinToString("\n"), "props")

  fun workerProps() = props(
    "application.workers.alphaWorker.batchSize=10",
    "application.workers.betaWorker.batchSize=20",
  )

  fun env(vararg vars: Pair<String, String>) =
    EnvironmentVariablesPropertySource(environmentVariableMap = { mapOf(*vars) })

  test("a higher-precedence file spelling a key differently keeps the lower-precedence file's spelling") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(props("application.workers.alpha-worker.batchSize=1234"))
      .addPropertySource(workerProps())
      .build()
      .loadConfigOrThrow<MapKeySpellingConfig>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("the spelling comes from the lowest of three sources that define the entry") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_ALPHAWORKER_BATCHSIZE" to "1234"))
      .addPropertySource(props("application.workers.alpha-worker.batchSize=99"))
      .addPropertySource(workerProps())
      .build()
      .loadConfigOrThrow<MapKeySpellingConfig>()

    config.application.workers shouldBe mapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("an env var source ranked below the file decides the spelling") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(workerProps())
      .addPropertySource(env("APPLICATION_WORKERS_ALPHAWORKER_BATCHSIZE" to "1234"))
      .build()
      .loadConfigOrThrow<MapKeySpellingConfig>()

    config.application.workers shouldBe mapOf(
      "ALPHAWORKER" to Issue603Worker(10),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("a map of primitives keeps the lower-precedence spelling of an overridden entry") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_SIZES_SMALLBOX" to "5", "APPLICATION_SIZES_hugeBox" to "100"))
      .addPropertySource(props("application.sizes.smallBox=1", "application.sizes.bigBox=2"))
      .build()
      .loadConfigOrThrow<MapKeySpellingIntConfig>()

    config.application.sizes shouldBe mapOf(
      "smallBox" to 5,
      "bigBox" to 2,
      "hugeBox" to 100,
    )
  }

  test("a LinkedHashMap keeps the lower-precedence spelling of an overridden entry") {
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_WORKERS_ALPHAWORKER_BATCHSIZE" to "1234"))
      .addPropertySource(workerProps())
      .build()
      .loadConfigOrThrow<MapKeySpellingLinkedConfig>()

    config.application.workers shouldBe linkedMapOf(
      "alphaWorker" to Issue603Worker(1234),
      "betaWorker" to Issue603Worker(20),
    )
  }

  test("the spelling survives a preprocessor that rebuilds the map") {
    val doubler = object : TraversingPrimitivePreprocessor() {
      override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> = when (node) {
        is StringNode -> node.copy(value = node.value.toIntOrNull()?.let { (it * 2).toString() } ?: node.value).valid()
        else -> node.valid()
      }
    }
    val config = ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPreprocessor(doubler)
      .addPropertySource(env("APPLICATION_SIZES_SMALLBOX" to "5"))
      .addPropertySource(props("application.sizes.smallBox=1", "application.sizes.bigBox=2"))
      .build()
      .loadConfigOrThrow<MapKeySpellingIntConfig>()

    config.application.sizes shouldBe mapOf("smallBox" to 10, "bigBox" to 4)
  }

  test("the report still shows the env var as the source key of the overriding value") {
    var report = ""
    ConfigLoaderBuilder.defaultWithoutPropertySources()
      .addPropertySource(env("APPLICATION_SIZES_SMALLBOX" to "5"))
      .addPropertySource(props("application.sizes.smallBox=1", "application.sizes.bigBox=2"))
      .withReport()
      .withReportPrintFn { report = it }
      .build()
      .loadConfigOrThrow<MapKeySpellingIntConfig>()
      .application.sizes shouldBe mapOf("smallBox" to 5, "bigBox" to 2)

    report.shouldContain("APPLICATION_SIZES_SMALLBOX")
    report.shouldContain("application.sizes.bigBox")
  }
})
