plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws2.regions)
   api(Libs.Aws2.secretsmanager)
}

apply("../publish.gradle.kts")
