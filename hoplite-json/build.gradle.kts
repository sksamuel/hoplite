plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Jackson.core)
   implementation(Libs.Jackson.databind)
}

apply("../publish.gradle.kts")
