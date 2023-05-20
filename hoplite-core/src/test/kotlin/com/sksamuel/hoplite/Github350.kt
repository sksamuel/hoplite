package com.sksamuel.hoplite

import com.sksamuel.hoplite.internal.CascadeMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalHoplite::class)
class Github350 : FunSpec() {
  init {
    test("CascadeMode.Override doesn't work #350") {

      val config: Config = ConfigLoader.builder()
        .addResourceOrFileSource("/.env.properties") // must always be last as it contains the super secret stuff and is ignored by git.
        .addResourceOrFileSource("/a1.properties")
        .addResourceOrFileSource("/application.properties") // the contents of this file should be ignored, but is not (see google credentials)
        .withCascadeMode(CascadeMode.Merge)
        .build()
        .loadConfigOrThrow()

      println("actual: $config")

      val expected = Config(
        GoogleCredentials(".env.properties", ".env.properties"),
        Database("a1.properties", "a1.properties")
      )

      config shouldBe expected
    }
  }
}

data class Config(
  val googleCredentials: GoogleCredentials,
  val database: Database
)

data class GoogleCredentials(
  val clientId: String,
  val clientSecret: String
)

data class Database(
  val jdbcUrl: String,
  val driverClassName: String
)
