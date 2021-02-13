plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  api(Libs.Hikari.core)

  testImplementation(project(":hoplite-yaml"))
  testImplementation("org.postgresql:postgresql:42.2.16")
}

apply("../publish.gradle.kts")
