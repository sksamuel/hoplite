plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Kotlin.coroutines)
   testImplementation(project(":hoplite-json"))
}

apply("../publish.gradle.kts")
