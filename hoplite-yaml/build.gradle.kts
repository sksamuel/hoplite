plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation(Libs.Snake.snakeyml)
}

apply("../publish.gradle.kts")
