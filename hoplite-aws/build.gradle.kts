plugins {
   kotlin("plugin.serialization").version("1.6.21")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.aws.java.sdk.core)
   api(libs.aws.java.sdk.ssm)
   api(libs.aws.java.sdk.secretsmanager)
   implementation(KotlinX.serialization.json)
   // TODO: Use new artifact as group changed.
   testApi("io.kotest:kotest-extensions-testcontainers:_")
//   moved:Testing.kotest.extensions.testContainers)
   testApi(libs.localstack)
   testApi(libs.logback.classic)
   testApi(libs.slf4j.api)
}

apply("../publish.gradle.kts")
