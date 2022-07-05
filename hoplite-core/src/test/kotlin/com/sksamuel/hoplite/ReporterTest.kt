package com.sksamuel.hoplite

import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.report.ReporterBuilder
import com.sksamuel.hoplite.report.resources
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.captureStandardOut
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ReporterTest : FunSpec({

  test("report node with default obfuscations") {

    val node = ConfigLoaderBuilder.default()
      .addPropertySource(
        PropertySource.string(
          """
          database.name = my database
          database.host = localhost
          database.port = 3306
          database.timeout = 100.0
          database.tls = true
          """.trimIndent(), "props"
        )
      )
      .build()
      .loadNodeOrThrow()

    Reporter.default().reportResources(node.resources(), "Used", emptySet()).trim() shouldBe """
Used keys 5
+------------------+---------------------+----------+
| Key              | Source              | Value    |
+------------------+---------------------+----------+
| database.host    | props string source | loc***** |
| database.name    | props string source | my ***** |
| database.port    | props string source | 3306     |
| database.timeout | props string source | 100.0    |
| database.tls     | props string source | true     |
+------------------+---------------------+----------+
""".trim()

  }

  test("report test with default obfuscations") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: Secret,
    )

    val builder = StringBuilder()

    ConfigLoaderBuilder.default()
      .addPropertySource(
        PropertySource.string(
          """
          name = my database
          host = localhost
          port = 3306
          password = ssm://mysecretkey
          """.trimIndent(), "props"
        )
      )
      .report(ReporterBuilder().withPrint(builder::append).build())
      .build()
      .loadConfigOrThrow<Test>()

    builder.toString().shouldContain(
      """
Used keys 4
+----------+---------------------+----------+
| Key      | Source              | Value    |
+----------+---------------------+----------+
| host     | props string source | loc***** |
| name     | props string source | my ***** |
| password | props string source | ssm***** |
| port     | props string source | 3306     |
+----------+---------------------+----------+
"""
    )

  }

  test("report sources") {
    Reporter.default().report(
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

  test("should print report on failed config") {

    data class Config(val name: String, val host: String, val port: Int)

    val out = captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          database.name = my database
          database.port = 3306
          """.trimIndent(), "props"
          )
        )
        .report()
        .build()
        .loadConfig<Config>()
    }

    out shouldContain """
+---------------+---------------------+----------+
| Key           | Source              | Value    |
+---------------+---------------------+----------+
| database.name | props string source | my ***** |
| database.port | props string source | 3306     |
+---------------+---------------------+----------+
""".trim()

  }
})
