plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.hikaricp)

   testImplementation(projects.hopliteYaml)
   testImplementation(libs.postgresql)
}

apply("../publish.gradle.kts")
