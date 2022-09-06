plugins {
   kotlin("plugin.serialization") version "1.6.21"
}

dependencies {
   api(projects.hopliteCore)
   api(Libs.Aws2.regions)
   api(Libs.Aws2.secretsmanager)
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}

apply("../publish.gradle.kts")
