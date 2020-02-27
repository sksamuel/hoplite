package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

data class EngineConfig(
  val env: String,
  val redis: RedisConfig,
  val odb: OdbConfig)

data class RedisConfig(
  val url: String,
  val requestChannel: String,
  val responseChannel: String)

data class OdbConfig(
  val home: String,
  val configFile: String,
  val dbFile: String,
  val server: Boolean)

class GithubIssue72 : StringSpec() {
  init {
    "NoSuchFieldError: Companion #72" {
      ConfigLoader().loadConfigOrThrow<EngineConfig>("/github72.yml") shouldBe
        EngineConfig(
          env = "production",
          redis = RedisConfig(url = "redis://localhost:6379/", requestChannel = "foo", responseChannel = "foo"),
          odb = OdbConfig(home = "db", configFile = "config/production.conf", dbFile = "engine.db", server = false)
        )
    }
  }
}
