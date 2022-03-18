package com.sksamuel.hoplite.yaml

import com.sksamuel.hoplite.ConfigLoaderBuilder
import io.kotest.core.spec.style.FreeSpec

data class A(
  val foo: Fooby = Fooby()
)

data class Fooby(
  val x: String = "x"
)

class Issue236Test : FreeSpec({

  val config = "{" +
    "}"

  "Bug Reproduce" {
    ConfigLoaderBuilder.default()
      .addSource(YamlPropertySource(config))
      .build()
      .loadConfigOrThrow<A>()
  }

})
