plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   testImplementation(projects.hopliteToml)
   testImplementation(projects.hopliteYaml)
   testImplementation(projects.hopliteJson)
   implementation(Libs.Vavr.kotlin)
}

apply("../publish.gradle.kts")
