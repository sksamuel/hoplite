dependencies {
   api(projects.hopliteCore)
   api(Libs.Azure.keyvault)
   api(Libs.Azure.identity)
   testApi(Libs.Kotest.testContainers)
}

apply("../publish.gradle.kts")
