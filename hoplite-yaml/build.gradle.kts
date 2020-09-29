plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation("org.yaml:snakeyaml:1.27")
}

apply("../publish.gradle.kts")
