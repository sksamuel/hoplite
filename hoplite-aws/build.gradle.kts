plugins {
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   api(libs.aws.java.sdk.secretsmanager)
   api(libs.aws.java.sdk.ssm)
   implementation(libs.kotlinx.serialization.json)
   testApi(libs.kotest.extensions.testcontainers)
   testApi(libs.testcontainers.localstack)
}

apply("../publish.gradle.kts")
