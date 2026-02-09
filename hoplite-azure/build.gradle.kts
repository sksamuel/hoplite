plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.azure.security.keyvault.secrets)
   api(libs.azure.identity)
}
