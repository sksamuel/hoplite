plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.tomlj)
}

apply("../publish.gradle.kts")
