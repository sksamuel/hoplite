dependencies {
   api(projects.hopliteCore)
   api(Libs.Google.secretsmanager)
   testApi(Libs.Kotest.testContainers)
}

apply("../publish.gradle.kts")
