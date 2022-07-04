plugins {
   kotlin("jvm")
   kotlin("plugin.serialization").version(Libs.kotlinVersion)
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Snake.snakeyaml)
   testImplementation("com.charleskorn.kaml", "kaml", "0.45.0")
}

apply("../publish.gradle.kts")
