plugins {
   kotlin("plugin.serialization").version("1.6.21")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.snakeyaml)
}

apply("../publish.gradle.kts")
