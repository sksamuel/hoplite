dependencies {
   api(projects.hopliteCore)
   api(libs.azure.security.keyvault.secrets)
   api(libs.azure.identity)
   // TODO: Use new artifact as group changed.
   // TODO: Use new artifact as group changed.
   testApi("io.kotest:kotest-extensions-testcontainers:_")
//   moved:Testing.kotest.extensions.testContainers)
}

apply("../publish.gradle.kts")
