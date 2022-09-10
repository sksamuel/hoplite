dependencies {
   api(projects.hopliteCore)
   implementation(libs.spring.vault.core)
   testApi(Testing.kotest.extensions.testContainers)
   testApi(libs.vault)
}

apply("../publish.gradle.kts")
