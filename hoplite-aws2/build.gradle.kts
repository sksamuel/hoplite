plugins {
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   api(libs.regions)
   api(libs.secretsmanager)
   implementation(libs.kotlinx.serialization.json)
   testApi("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
   testApi(libs.testcontainers.localstack)
}

apply("../publish.gradle.kts")
