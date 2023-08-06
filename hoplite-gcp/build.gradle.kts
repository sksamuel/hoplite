dependencies {
   api(projects.hopliteCore)
   api(libs.google.cloud.secretmanager)
   testApi("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
}

apply("../publish.gradle.kts")
