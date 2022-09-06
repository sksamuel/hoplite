dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Vault.core)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:vault:_")
}

apply("../publish.gradle.kts")
