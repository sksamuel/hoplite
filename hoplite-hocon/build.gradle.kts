plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  implementation("com.typesafe:config:1.4.0")
}

apply("../publish.gradle.kts")
