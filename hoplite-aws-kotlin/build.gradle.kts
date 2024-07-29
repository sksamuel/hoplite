plugins {
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   api(libs.aws.kotlin.secretsmanager)
   api(libs.aws.kotlin.ssm)
   api(libs.regions)
   implementation(libs.kotlinx.serialization.json)
   testApi(libs.kotest.extensions.testcontainers)
   testApi(libs.testcontainers.localstack)
}

apply("../publish.gradle.kts")
