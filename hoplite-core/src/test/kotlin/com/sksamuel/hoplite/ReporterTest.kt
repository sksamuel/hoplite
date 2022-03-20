package com.sksamuel.hoplite

import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.report.resources
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ReporterTest : FunSpec({

  test("report node") {

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

    Reporter().reportPaths(node.resources(), "Used").trim() shouldBe """
Used keys 4
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

  test("report sources") {
    Reporter().report(
      ConfigLoaderBuilder.default()
        .addEnvironmentSource()
        .build()
        .propertySources
    ).trim() shouldBe """
Property sources (highest to lowest priority):
  - System Properties
  - ${System.getProperty("user.home")}/.userconfig.<ext>
  - Env Var
""".trimIndent().trim()

  }

})
