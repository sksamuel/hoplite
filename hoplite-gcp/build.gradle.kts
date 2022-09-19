dependencies {
   api(projects.hopliteCore)
   api(libs.google.cloud.secretmanager)
   testImplementation(Testing.kotest.extensions.testContainers)
}

apply("../publish.gradle.kts")
