package com.sksamuel.hoplite

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ReporterTest : FunSpec({

  test("reporter happy path") {

    val node = ConfigLoaderBuilder.default()
      .addPropertySource(
        PropertySource.string(
          """
          database.name = my database
          database.host = localhost
          database.port = 3306
          database.password.masked = ssm://mysecretkey
          """.trimIndent(), "props"
        )
      )
      .build()
      .loadNodeOrThrow()

    Reporter().report(node).trim() shouldBe """
+--------------------------+---------------------+-------------------+
| Key                      | Source              | Value             |
+--------------------------+---------------------+-------------------+
| database.password.masked | props string source | ssm://mysecretkey |
| database.port            | props string source | 3306              |
| database.host            | props string source | localhost         |
| database.name            | props string source | my database       |
+--------------------------+---------------------+-------------------+
""".trim()

  }

})
