dependencies {
   api(projects.hopliteCore)
   testImplementation(projects.hopliteToml)
   testImplementation(projects.hopliteYaml)
   testImplementation(projects.hopliteJson)
   implementation(libs.vavr.kotlin)
}

apply("../publish.gradle.kts")
