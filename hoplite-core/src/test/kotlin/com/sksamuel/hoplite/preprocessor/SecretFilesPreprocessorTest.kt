package com.sksamuel.hoplite.preprocessor

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.PropertySource
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

class SecretFilesPreprocessorTest : FunSpec({

  fun File.writeSecret(name: String, value: String) {
    resolve(name).writeText(value)
  }

  test("resolves a single \${secret:key} placeholder from a file") {
    val secrets = tempdir().apply { writeSecret("apiKey", "abc-123") }

    data class Config(val apiKey: String)

    val config = ConfigLoaderBuilder.default()
      .addPreprocessor(SecretFilesPreprocessor(secrets.toPath()))
      .addPropertySource(PropertySource.string("apiKey=\${secret:apiKey}", "props"))
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(apiKey = "abc-123")
  }

  test("resolves multiple secrets from separate files within the same config") {
    val secrets = tempdir().apply {
      writeSecret("apiKey", "abc-123")
      writeSecret("dbPassword", "hunter2")
      writeSecret("oauthClientSecret", "client-shhh")
    }

    data class Db(val user: String, val password: String)
    data class Config(val apiKey: String, val oauth: String, val db: Db)

    val config = ConfigLoaderBuilder.default()
      .addPreprocessor(SecretFilesPreprocessor(secrets.toPath()))
      .addPropertySource(
        PropertySource.string(
          """
          apiKey=${'$'}{secret:apiKey}
          oauth=${'$'}{secret:oauthClientSecret}
          db.user=admin
          db.password=${'$'}{secret:dbPassword}
          """.trimIndent(),
          "props"
        )
      )
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(
      apiKey = "abc-123",
      oauth = "client-shhh",
      db = Db(user = "admin", password = "hunter2"),
    )
  }

  test("strips a single trailing newline from secret files (\\n and \\r\\n)") {
    val secrets = tempdir().apply {
      writeSecret("unix", "unix-value\n")
      writeSecret("windows", "windows-value\r\n")
      writeSecret("none", "no-newline")
    }

    data class Config(val unix: String, val windows: String, val none: String)

    val config = ConfigLoaderBuilder.default()
      .addPreprocessor(SecretFilesPreprocessor(secrets.toPath()))
      .addPropertySource(
        PropertySource.string(
          """
          unix=${'$'}{secret:unix}
          windows=${'$'}{secret:windows}
          none=${'$'}{secret:none}
          """.trimIndent(),
          "props"
        )
      )
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(unix = "unix-value", windows = "windows-value", none = "no-newline")
  }

  test("supports multiple secret references within a single value") {
    val secrets = tempdir().apply {
      writeSecret("user", "alice")
      writeSecret("host", "db.internal")
    }

    data class Config(val url: String)

    val config = ConfigLoaderBuilder.default()
      .addPreprocessor(SecretFilesPreprocessor(secrets.toPath()))
      .addPropertySource(
        PropertySource.string("url=jdbc://\${secret:user}@\${secret:host}/db", "props")
      )
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(url = "jdbc://alice@db.internal/db")
  }

  test("accepts a string base path via the convenience constructor") {
    val secrets = tempdir().apply { writeSecret("apiKey", "abc-123") }

    data class Config(val apiKey: String)

    val config = ConfigLoaderBuilder.default()
      .addPreprocessor(SecretFilesPreprocessor(secrets.absolutePath))
      .addPropertySource(PropertySource.string("apiKey=\${secret:apiKey}", "props"))
      .build()
      .loadConfigOrThrow<Config>()

    config shouldBe Config(apiKey = "abc-123")
  }
})
