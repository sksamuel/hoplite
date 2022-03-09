plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation(Libs.Snake.snakeyaml)
}

apply("../publish.gradle.kts")
