package com.sksamuel.hoplite.watcher.consul

import com.orbitz.consul.Consul
import com.pszymczyk.consul.ConsulProcess
import com.pszymczyk.consul.ConsulStarterBuilder
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.consul.ConsulConfigPreprocessor
import com.sksamuel.hoplite.watch.ReloadableConfig
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.framework.concurrency.eventually
import io.kotest.matchers.shouldBe

data class TestConfig(val foo: String)

@ExperimentalKotest
class ConsulWatcherTest: FunSpec({
  lateinit var consul : ConsulProcess

  beforeSpec {
    consul = ConsulStarterBuilder.consulStarter().buildAndStart()
  }

  afterSpec {
    consul.close()
  }

  test("Can reload values from a consul cache") {
    val embeddedConsulURL = "http://localhost:${consul.httpPort}"
    val kvClient = Consul.builder()
      .withUrl(embeddedConsulURL)
      .build()
      .keyValueClient()
    kvClient.putValue("foo", "bar")

    val configLoader = ConfigLoaderBuilder.default()
      .addSource(PropertySource.resource("/consulConfig.yml"))
      .addPreprocessor(ConsulConfigPreprocessor(embeddedConsulURL))
      .build()

    val reloadableConfig = ReloadableConfig(configLoader, TestConfig::class)
      .addWatcher(ConsulWatcher(kvClient, listOf("foo")))

    configLoader.loadConfigOrThrow<TestConfig>()
    var latest = reloadableConfig.getLatest()
    latest.foo shouldBe "bar"

    kvClient.putValue("foo", "baz")

    eventually(2000) {
      latest = reloadableConfig.getLatest()
      latest.foo shouldBe "baz"
    }
  }
})

