package com.sksamuel.hoplite.watch

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.PropertySourceContext
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.framework.concurrency.eventually
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay

class TestWatcher: Watchable {
  private var cb: (() -> Unit)? = null
  override fun watch(callback: () -> Unit, errorHandler: (Throwable) -> Unit) {
    cb = callback
  }

  fun update() {
    cb?.invoke()
  }
}

data class TestConfig(val foo: String)

@ExperimentalKotest
class WatcherTest : FunSpec({

  test("should throw on startup if there is an initial error") {

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.resource("does-not-exist.yml"))

    val watcher = TestWatcher()
    shouldThrowAny {
      ReloadableConfig(configLoader.build(), TestConfig::class)
        .addWatcher(watcher)
        .getLatest()
    }
  }

  test("will call the provided error handler if reloadConfig throws on a refresh") {

    val map = mutableMapOf("foo" to "bar")
    var firsttime = true
    val onetimesource = object : PropertySource {
      override fun node(context: PropertySourceContext): ConfigResult<Node> {
        return if (firsttime) {
          firsttime = false
          PropertySource.map(map).node(context)
        } else {
          ConfigFailure.UnknownSource("boom").invalid()
        }
      }
    }

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(onetimesource)

    val watcher = TestWatcher()
    var error: Throwable? = null
    ReloadableConfig(configLoader.build(), TestConfig::class)
      .addWatcher(watcher)
      .addErrorHandler { error = it }

    watcher.update()

    error shouldNotBe null
    error?.message shouldContain "Error loading config"
  }

  test("will reload the config when the watchable triggers an update") {
    val map = mutableMapOf("foo" to "bar")
    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.map(map))

    val watcher = TestWatcher()
    val reloadableConfig = ReloadableConfig(configLoader.build(), TestConfig::class)
      .addWatcher(watcher)

    val config = reloadableConfig.getLatest()
    config.foo shouldBe "bar"

    map["foo"] = "baz"
    watcher.update()

    val reloadedConfig = reloadableConfig.getLatest()
    reloadedConfig shouldNotBe null
    reloadedConfig.foo shouldBe "baz"
  }

  test("FileWatcher will reload if a file in the specified directory changes") {
    val tmpFile = tempfile("file", ".json")
    tmpFile.writeText("""{"foo": "bar"}""")

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.file(tmpFile))
      .build()

    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(FileWatcher(tmpFile.parent))

    val config = reloadableConfig.getLatest()
    config.foo shouldBe "bar"

    delay(1000)
    tmpFile.writeText("""{"foo": "baz"}""")

    eventually(10000) {
      val reloadedConfig = reloadableConfig.getLatest()
      reloadedConfig shouldNotBe null
      reloadedConfig.foo shouldBe "baz"
    }
  }
})
