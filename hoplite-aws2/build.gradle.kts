plugins {
   kotlin("plugin.serialization").version("1.9.25")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.regions)
   api(libs.secretsmanager)
   implementation(libs.kotlinx.serialization.json)
}

apply("../publish.gradle.kts")
