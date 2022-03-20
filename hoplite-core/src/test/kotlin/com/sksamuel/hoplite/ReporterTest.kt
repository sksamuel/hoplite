package com.sksamuel.hoplite

import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.report.ReporterBuilder
import com.sksamuel.hoplite.report.resources
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ReporterTest : FunSpec({

  test("report node") {

    val node = ConfigLoaderBuilder.default()
      .addPropertySource(
        PropertySource.string(
          """
          database.name = my database
          database.host = localhost
          database.port = 3306
          """.trimIndent(), "props"
        )
      )
      .build()
      .loadNodeOrThrow()

    Reporter.default().reportPaths(node.resources(), "Used", emptySet()).trim() shouldBe """
Used keys 3
+---------------+---------------------+-------------+
| Key           | Source              | Value       |
+---------------+---------------------+-------------+
| database.port | props string source | 3306        |
| database.host | props string source | localhost   |
| database.name | props string source | my database |
+---------------+---------------------+-------------+
""".trim()

  }

  test("report node with obfuscation based on field name") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: Secret,
    )

    val builder = StringBuilder()

    val node = ConfigLoaderBuilder.default()
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

    builder.toString().shouldContain("""
Used keys 4
+----------+---------------------+-------------+
| Key      | Source              | Value       |
+----------+---------------------+-------------+
| password | props string source | ss*****     |
| port     | props string source | 3306        |
| host     | props string source | localhost   |
| name     | props string source | my database |
+----------+---------------------+-------------+
""")

  }

  test("report with obfuscation based on secret type") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val wobble: Secret,
    )

    val builder = StringBuilder()

    ConfigLoaderBuilder.default()
      .addPropertySource(
        PropertySource.string(
          """
            name = my database
            host = localhost
            port = 3306
            wobble = hideme
          """.trimIndent(), "props"
        )
      )
      .report(ReporterBuilder().withPrint(builder::append).build())
      .build()
      .loadConfigOrThrow<Test>()

    builder.toString().shouldContain("""
Used keys 4
+--------+---------------------+-------------+
| Key    | Source              | Value       |
+--------+---------------------+-------------+
| port   | props string source | 3306        |
| host   | props string source | localhost   |
| name   | props string source | my database |
| wobble | props string source | hi*****     |
+--------+---------------------+-------------+
""")

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

})
