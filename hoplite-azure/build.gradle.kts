dependencies {
   api(projects.hopliteCore)
   api(libs.azure.security.keyvault.secrets)
   api(libs.azure.identity)
   testApi("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
}

apply("../publish.gradle.kts")
