plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  testImplementation(project(":hoplite-toml"))
  testImplementation(project(":hoplite-yaml"))
  testImplementation(project(":hoplite-json"))
  implementation("io.arrow-kt:arrow-validation:0.11.0")
  implementation("io.arrow-kt:arrow-core-data:0.11.0")
}

apply("../publish.gradle.kts")
