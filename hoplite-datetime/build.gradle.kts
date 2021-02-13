plugins {
  kotlin("jvm")
}

repositories {
  maven(url = "https://kotlin.bintray.com/kotlinx/") // soon will be just jcenter()
}

dependencies {
  api(project(":hoplite-core"))
  implementation(Libs.Kotlin.datetime)
}

apply("../publish.gradle.kts")
