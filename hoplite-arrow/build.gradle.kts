plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  testImplementation(project(":hoplite-toml"))
  testImplementation(project(":hoplite-yaml"))
  testImplementation(project(":hoplite-json"))
  implementation(Libs.Arrow.Core)
}

apply("../publish.gradle.kts")
