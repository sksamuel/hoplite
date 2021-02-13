plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation(Libs.Toml.toml)
}

apply("../publish.gradle.kts")
