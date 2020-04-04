package com.sksamuel.hoplite.hocon

import com.sksamuel.hoplite.ConfigLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe

data class Mongo(val hostname: String)
data class Conf(val mongo: Mongo)

class HoconEnvOverrideTest : FunSpec({

  test("override hocon configuration with env variables") {
    withEnvironment("MONGO_HOSTNAME", "foo.wibble.com") {
      val config = ConfigLoader().loadConfigOrThrow<Conf>("/hocon.conf")
      config.mongo.hostname shouldBe "foo.wibble.com"
    }
  }

})
