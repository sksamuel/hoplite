plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Kotlin.datetime)
}

apply("../publish.gradle.kts")
