plugins {
  kotlin("jvm")
}

dependencies {
  api(projects.hopliteCore)
  testImplementation(projects.hopliteToml)
  testImplementation(projects.hopliteYaml)
  testImplementation(projects.hopliteJson)
  implementation(Libs.Arrow.Core)
}

apply("../publish.gradle.kts")
