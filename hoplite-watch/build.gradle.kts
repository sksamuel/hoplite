plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(project(":hoplite-yaml"))

   implementation(Libs.Kotlin.coroutines)
   implementation(Libs.Snake.snakeyml)
   implementation(Libs.Orbitz.consul)

   testImplementation(project(":hoplite-json"))
   testImplementation(project(":hoplite-consul"))
   testImplementation(Libs.EmbeddedConsul.consul)
}


apply("../publish.gradle.kts")
