dependencies {
   api(projects.hopliteCore)
   api(libs.azure.security.keyvault.secrets)
   api(libs.azure.identity)
   testApi(Testing.kotest.extensions.testContainers)
}

apply("../publish.gradle.kts")
