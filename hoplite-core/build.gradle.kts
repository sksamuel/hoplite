plugins {
  kotlin("jvm")
}

dependencies {
  api(Libs.Kotlin.reflect)
}

apply("../publish.gradle.kts")
