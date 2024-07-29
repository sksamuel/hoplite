dependencies {
   api(projects.hopliteCore)
   implementation(libs.spring.vault.core)
   testApi(libs.kotest.extensions.testcontainers)
   testApi(libs.testcontainers.vault)
}

apply("../publish.gradle.kts")
