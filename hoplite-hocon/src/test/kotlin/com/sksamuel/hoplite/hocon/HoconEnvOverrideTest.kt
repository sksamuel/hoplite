package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

data class Mongo(val hostname: String)
data class Conf(val mongo: Mongo)

class HoconEnvOverrideTest : FunSpec({

  test("override hocon configuration with env variables") {
    val config = ConfigLoader().loadConfigOrThrow<Conf>("/hocon.conf")
    config.mongo.hostname shouldBe "localhost"

    withEnvironment("MONGO_HOSTNAME", "foo.wibble.com") {
      val config = ConfigLoader().loadConfigOrThrow<Conf>("/hocon.conf")
      config.mongo.hostname shouldBe "foo.wibble.com" // as of release 2.9.0 (latest as of 2026-02-18) this fails. This is fixed on latest master
    }
  }

})
