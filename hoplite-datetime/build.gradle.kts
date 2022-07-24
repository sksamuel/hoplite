plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Kotlin.datetime)
}

apply("../publish.gradle.kts")
