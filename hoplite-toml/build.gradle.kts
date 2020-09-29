plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation("org.tomlj:tomlj:1.0.0")
}

apply("../publish.gradle.kts")
