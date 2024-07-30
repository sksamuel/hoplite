plugins {
   kotlin("plugin.serialization").version("1.6.21")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.aws.java.sdk.secretsmanager)
   api(libs.aws.java.sdk.ssm)
   implementation(libs.kotlinx.serialization.json)
   testApi("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
   testApi(libs.testcontainers.localstack)
}

apply("../publish.gradle.kts")
