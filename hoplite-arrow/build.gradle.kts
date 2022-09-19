dependencies {
  api(projects.hopliteCore)
  testImplementation(projects.hopliteToml)
  testImplementation(projects.hopliteYaml)
  testImplementation(projects.hopliteJson)
  implementation(Arrow.core)
}

apply("../publish.gradle.kts")
