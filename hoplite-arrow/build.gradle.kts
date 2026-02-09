plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.hopliteCore)
   testImplementation(projects.hopliteToml)
   testImplementation(projects.hopliteYaml)
   testImplementation(projects.hopliteJson)
   implementation(libs.arrow.core)
}
