plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Kotlin.coroutines)

   testImplementation(project(":hoplite-json"))
}


apply("../publish.gradle.kts")
