dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   api(Libs.Azure.keyvault)
   api(Libs.Azure.identity)
   testApi(Libs.Kotest.testContainers)
}

apply("../publish.gradle.kts")
