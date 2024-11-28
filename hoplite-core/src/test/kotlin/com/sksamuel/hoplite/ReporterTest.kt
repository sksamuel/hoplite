package com.sksamuel.hoplite

import com.sksamuel.hoplite.env.Environment
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.preprocessor.TraversingPrimitivePreprocessor
import com.sksamuel.hoplite.report.Reporter
import com.sksamuel.hoplite.secrets.StandardSecretsPolicy
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.captureStandardOut
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(ExperimentalHoplite::class)
class ReporterTest : FunSpec({

  test("report test with default obfuscations") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: Secret
    )

    captureStandardOut {
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
        .withReport()
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain(
      """
Used keys: 4
+----------+---------------------+------------+----------+
| Key      | Source              | Source Key | Value    |
+----------+---------------------+------------+----------+
| host     | props string source | host       | loc***** |
| name     | props string source | name       | my ***** |
| password | props string source | password   | ssm***** |
| port     | props string source | port       | 3306     |
+----------+---------------------+------------+----------+
"""
    )

  }

  test("report sources") {
    val xdg = "\$XDG_CONFIG_HOME"
    Reporter.default().report(
      ConfigLoaderBuilder.default()
        .build()
        .propertySources
    ).trim() shouldBe """
Property sources (highest to lowest priority):
  - Env Var
  - System Properties
  - ${System.getProperty("user.home")}/.userconfig.<ext>
  - $xdg/hoplite.<ext>
""".trimIndent().trim()

  }

  test("report should remove new lines") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: Secret
    )

    captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          name = my database
          host = l\nremotehost
          port = 3306
          password = ssm://mysecretkey
          """.trimIndent(), "props"
          )
        )
        .withReport()
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain(
      """
Used keys: 4
+----------+---------------------+------------+----------+
| Key      | Source              | Source Key | Value    |
+----------+---------------------+------------+----------+
| host     | props string source | host       | lr*****  |
| name     | props string source | name       | my ***** |
| password | props string source | password   | ssm***** |
| port     | props string source | port       | 3306     |
+----------+---------------------+------------+----------+
"""
    )

  }

  test("should print report on failed config") {

    data class Config(val name: String, val host: String, val port: Int)

    val out = captureStandardOut {
      ConfigLoaderBuilder.defaultWithoutPropertySources()
        .addPropertySource(
          PropertySource.string(
            """
          database.name = my database
          database.port = 3306
          """.trimIndent(), "props"
          )
        )
        .withReport()
        .build()
        .loadConfig<Config>()
    }

    out shouldContain """
+---------------+---------------------+---------------+----------+
| Key           | Source              | Source Key    | Value    |
+---------------+---------------------+---------------+----------+
| database.name | props string source | database.name | my ***** |
| database.port | props string source | database.port | 3306     |
+---------------+---------------------+---------------+----------+
""".trim()

  }

  test("withReport should use secrets policy") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: String
    )

    captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(
          PropertySource.string(
            """
          name = my database
          host = localhost
          port = 3306
          password = gcpsm://mysecretkey2
          """.trimIndent(), "props"
          )
        )
        .withReport()
        .addPreprocessor(object : TraversingPrimitivePreprocessor() {
          override fun handle(node: PrimitiveNode, context: DecoderContext): ConfigResult<Node> {
            return if (node is StringNode && node.value.startsWith("gcpsm://"))
              node.withMeta(CommonMetadata.UnprocessedValue, "gcpsm://mysecretkey2").valid()
            else
              node.valid()
          }
        })
        .withSecretsPolicy(StandardSecretsPolicy)
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain(
      """
Used keys: 4
+----------+---------------------+------------+-------------+
| Key      | Source              | Source Key | Value       |
+----------+---------------------+------------+-------------+
| host     | props string source | host       | localhost   |
| name     | props string source | name       | my database |
| password | props string source | password   | gcp*****    |
| port     | props string source | port       | 3306        |
+----------+---------------------+------------+-------------+

"""
    )
  }

  test("withReport should output env") {

    data class Test(
      val a: String
    )

    captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.map(mapOf("a" to "foo")))
        .withEnvironment(Environment.staging)
        .withReport()
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain(
      """--Start Hoplite Config Report---

Environment: staging

Property sources (highest to lowest priority):"""
    )
  }

  test("withReport should include report sections") {

    data class Test(
      val a: String
    )

    captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.map(mapOf("a" to "foo")))
        .addPreprocessor { node, context ->
          context.report("AWS Secrets Manager Lookups", mapOf("foo" to "a", "bar" to "b"))
          context.report("AWS Secrets Manager Lookups", mapOf("foo" to null, "bar" to "big long extra wide value"))
          context.report("AWS Secrets Manager Lookups", mapOf("foo" to "e", "big fat title" to "f"))
          context.report("Vault Lookups", mapOf("foo" to "e", "baz" to "f"))
          node.valid()
        }
        .withEnvironment(Environment.staging)
        .withReport()
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain("""AWS Secrets Manager Lookups
+-----+---------------------------+---------------+
| foo | bar                       | big fat title |
+-----+---------------------------+---------------+
| a   | b                         |               |
|     | big long extra wide value |               |
| e   |                           | f             |
+-----+---------------------------+---------------+

Vault Lookups
+-----+-----+
| foo | baz |
+-----+-----+
| e   | f   |
+-----+-----+""")
  }
})
