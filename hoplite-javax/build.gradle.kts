plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   testImplementation(projects.hopliteToml)
   testImplementation(projects.hopliteYaml)
   testImplementation(projects.hopliteJson)
}
