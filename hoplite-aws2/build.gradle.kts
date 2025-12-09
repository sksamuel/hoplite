plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   api(libs.regions)
   api(libs.secretsmanager)
   implementation(libs.kotlinx.serialization.json)
   testApi(libs.testcontainers.localstack)
}
