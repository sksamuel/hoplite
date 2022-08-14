dependencies {
   api(project(":hoplite-core"))
   api(Libs.Google.secretsmanager)
   testApi(Libs.Kotest.testContainers)
}

apply("../publish.gradle.kts")
