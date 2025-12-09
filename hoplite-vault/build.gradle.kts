plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.spring.vault.core)
   testApi(libs.testcontainers.vault)
}
