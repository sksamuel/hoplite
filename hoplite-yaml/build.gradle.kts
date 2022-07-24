plugins {
   kotlin("jvm")
   kotlin("plugin.serialization").version(Libs.kotlinVersion)
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Snake.snakeyaml)
   testImplementation(Libs.CharlesKorn.kaml)
}

apply("../publish.gradle.kts")
