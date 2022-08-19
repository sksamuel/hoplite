plugins {
   kotlin("plugin.serialization") version "1.6.21"
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws2.regions)
   api(Libs.Aws2.secretsmanager)
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

apply("../publish.gradle.kts")
