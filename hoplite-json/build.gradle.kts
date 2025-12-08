plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.jackson.core)
   implementation(libs.jackson.databind)
}
