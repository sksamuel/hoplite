plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation(Libs.Typesafe.config)
}

apply("../publish.gradle.kts")
