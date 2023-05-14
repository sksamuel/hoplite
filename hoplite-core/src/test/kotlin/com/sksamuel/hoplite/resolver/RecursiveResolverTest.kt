package com.sksamuel.hoplite.resolver

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.withSystemProperty
import io.kotest.matchers.shouldBe
import java.util.Properties

class RecursiveResolverTest : StringSpec() {
  init {

    data class Config(val result: String)

    "should resolve recursively" {

      val props = Properties()
      props["foo"] = "ar"
      props["bar"] = "az"
      props["baz"] = "oat"
      props["result"] = "b\${{ b\${{ b\${{ foo }} }} }}ymcb\${{ b\${{ b\${{ foo }} }} }}face"

      val config = ConfigLoaderBuilder.create()
        .addPropertySource(PropsPropertySource(props))
        .build()
        .loadConfigOrThrow<Config>()

      config shouldBe Config(result = "boatymcboatface")
    }

    "should mix and match resolvers" {

      val props = Properties()
      props["foo"] = "mc"
      props["result"] = "boaty\${{ foo }}boat\${{sysprop.bar}}"

      val config = withSystemProperty("bar", "face") {
        ConfigLoaderBuilder.create()
          .addPropertySource(PropsPropertySource(props))
          .build()
          .loadConfigOrThrow<Config>()
      }

      config shouldBe Config(result = "boatymcboatface")
    }
  }
}
