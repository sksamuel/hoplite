dependencies {
   api(projects.hopliteCore)
   api(libs.google.cloud.secretmanager)
   // TODO: Use new artifact as group changed.
   testApi("io.kotest:kotest-extensions-testcontainers:_")
//   moved:Testing.kotest.extensions.testContainers)
}

apply("../publish.gradle.kts")
