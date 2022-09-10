plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.jackson.core)
   implementation(libs.jackson.databind)
}

apply("../publish.gradle.kts")
