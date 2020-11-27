plugins {
  kotlin("jvm")
}

repositories {
  maven(url = "https://kotlin.bintray.com/kotlinx/") // soon will be just jcenter()
}

dependencies {
  api(project(":hoplite-core"))
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
}

apply("../publish.gradle.kts")
