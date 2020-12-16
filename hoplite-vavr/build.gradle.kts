plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  testImplementation(project(":hoplite-toml"))
  testImplementation(project(":hoplite-yaml"))
  testImplementation(project(":hoplite-json"))
  implementation(Libs.Vavr.kotlin)
}

apply("../publish.gradle.kts")
