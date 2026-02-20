plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.hikaricp)

   testImplementation(projects.hopliteYaml)
   testImplementation(libs.postgresql)
}
