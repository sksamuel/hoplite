plugins {
   kotlin("plugin.serialization") version "1.6.21"
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws2.regions)
   api(Libs.Aws2.secretsmanager)
}

apply("../publish.gradle.kts")
