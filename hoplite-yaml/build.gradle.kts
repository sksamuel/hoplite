plugins {
   kotlin("plugin.serialization").version("1.6.21")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.snakeyaml)
   testImplementation(libs.kaml)
}

apply("../publish.gradle.kts")
