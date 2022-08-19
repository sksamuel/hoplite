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
      val password: Secret,
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
    val xdg = "\$XDG_CONFIG_HOME"
    Reporter.default().report(
      ConfigLoaderBuilder.default()
        .addEnvironmentSource()
        .build()
        .propertySources
    ).trim() shouldBe """
Property sources (highest to lowest priority):
  - Env Var Overrides
  - System Properties
  - ${System.getProperty("user.home")}/.userconfig.<ext>
  - $xdg/hoplite.<ext>
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
        .withReport()
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

  test("withReport should include remote lookups") {

    data class Test(
      val name: String,
      val host: String,
      val port: Int,
      val password: String,
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
          override fun handle(node: PrimitiveNode): ConfigResult<Node> {
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
+----------+---------------------+-------------+----------------------+
| Key      | Source              | Value       | Unprocessed Value    |
+----------+---------------------+-------------+----------------------+
| host     | props string source | localhost   |                      |
| name     | props string source | my database |                      |
| password | props string source | gcp*****    | gcpsm://mysecretkey2 |
| port     | props string source | 3306        |                      |
+----------+---------------------+-------------+----------------------+

"""
    )
  }

  test("withReport should output env") {

    data class Test(
      val a: String,
    )

    captureStandardOut {
      ConfigLoaderBuilder.default()
        .addPropertySource(PropertySource.map(mapOf("a" to "foo")))
        .withEnvironment(Environment.staging)
        .withReport()
        .build()
        .loadConfigOrThrow<Test>()
    }.shouldContain("""--Start Hoplite Config Report---

Environment: staging

Property sources (highest to lowest priority):""")
  }
})
