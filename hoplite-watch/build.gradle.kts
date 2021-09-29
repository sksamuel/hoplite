plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Kotlin.coroutines)
   implementation(Libs.Snake.snakeyml)

   testImplementation(project(":hoplite-json"))
}


apply("../publish.gradle.kts")
