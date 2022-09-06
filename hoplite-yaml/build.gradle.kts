plugins {
   kotlin("plugin.serialization").version(Libs.kotlinVersion)
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Snake.snakeyaml)
   testImplementation(Libs.CharlesKorn.kaml)
}

apply("../publish.gradle.kts")
