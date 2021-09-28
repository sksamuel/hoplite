package com.sksamuel.hoplite.watch

import com.orbitz.consul.Consul
import com.orbitz.consul.cache.KVCache
import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarterBuilder
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.consul.ConsulConfigPreprocessor
import com.sksamuel.hoplite.watch.watchers.ConsulWatcher
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.framework.concurrency.eventually
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.time.ExperimentalTime


class TestWatcher: Watchable {
  var cb: (() -> Unit)? = null
  override fun watch(callback: () -> Unit) {
    cb = callback
  }

  fun update() {
    cb?.invoke()
  }
}

data class TestConfig(val foo: String)

@ExperimentalKotest
@OptIn(ExperimentalTime::class)
class WatcherTest : FunSpec({
  lateinit var consul : ConsulProcess

  beforeSpec {
    consul = ConsulStarterBuilder.consulStarter().buildAndStart()
  }

  afterSpec {
    consul.close()
  }

  test("will reload the config when the watchable triggers an update") {
    val map = mutableMapOf("foo" to "bar")
    val configLoader = ConfigLoader.Builder()
      .addSource(PropertySource.map(map))

    val watcher = TestWatcher()
    val reloadableConfig = ReloadableConfig(configLoader.build(), TestConfig::class)
      .addWatcher(watcher)

    val config = reloadableConfig.getLatest()
    config?.foo shouldBe "bar"

    map["foo"] = "baz"
    watcher.update()
    val reloadedConfig = reloadableConfig.getLatest()
    reloadedConfig shouldNotBe null
    reloadedConfig?.foo shouldBe "baz"
  }

  test("FileWatcher will reload if a file in the specified directory changes") {
    val tmpFile = tempfile("file", ".json")
    tmpFile.writeText("""{"foo": "bar"}""")

    val configLoader = ConfigLoader.Builder()
      .addSource(PropertySource.file(tmpFile))
      .build()

    val c1 = configLoader.loadConfigOrThrow<TestConfig>()
    println("config: " + c1.foo)

    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(FileWatcher(tmpFile.parent))

    val config = reloadableConfig.getLatest()
    config?.foo shouldBe "bar"

    Thread.sleep(2000)
    tmpFile.writeText("""{"foo": "baz"}""")
    Thread.sleep(2000)

    eventually(10000) {
      val reloadedConfig = reloadableConfig.getLatest()
      reloadedConfig shouldNotBe null
      reloadedConfig?.foo shouldBe "baz"
    }
  }

  test("Can reload values from a consul cache") {
    val kvClient = Consul.builder()
      .withUrl("http://localhost:${consul.httpPort}")
      .build()
      .keyValueClient()
    kvClient.putValue("foo", "bar")

    val configLoader = ConfigLoader.Builder()
      .addSource(PropertySource.resource("/consulConfig.yml"))
      .addPreprocessor(ConsulConfigPreprocessor("http://localhost:${consul.httpPort}"))
      .build()

    val kvCache = KVCache.newCache(kvClient, "foo", 3)
    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(ConsulWatcher(kvCache))

    var latest = reloadableConfig.getLatest()
    latest?.foo shouldBe "bar"

    kvClient.putValue("foo", "baz")

    eventually(2000) {
      latest = reloadableConfig.getLatest()
      latest?.foo shouldBe "baz"
    }
  }
})
