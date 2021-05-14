plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  api(Libs.Hikari.core)

  testImplementation(project(":hoplite-yaml"))
  testImplementation(Libs.Postgres.driver)
}

apply("../publish.gradle.kts")
