plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   api(libs.aws.kotlin.secretsmanager)
   api(libs.aws.kotlin.ssm)
   implementation(libs.kotlinx.serialization.json)
   testApi(libs.testcontainers.localstack)
}
