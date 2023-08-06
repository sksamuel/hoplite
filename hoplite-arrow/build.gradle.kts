dependencies {
  api(projects.hopliteCore)
  testImplementation(projects.hopliteToml)
  testImplementation(projects.hopliteYaml)
  testImplementation(projects.hopliteJson)
  implementation(libs.arrow.core)
}

apply("../publish.gradle.kts")
