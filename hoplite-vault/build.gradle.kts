dependencies {
   api(projects.hopliteCore)
   implementation(libs.spring.vault.core)
   // TODO: Use new artifact as group changed.
   testApi("io.kotest:kotest-extensions-testcontainers:_")
//   moved:Testing.kotest.extensions.testContainers)
   testApi(libs.vault)
}

apply("../publish.gradle.kts")
