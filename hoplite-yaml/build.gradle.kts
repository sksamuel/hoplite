plugins {
   kotlin("plugin.serialization").version("1.9.25")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.snakeyaml)
}

apply("../publish.gradle.kts")
