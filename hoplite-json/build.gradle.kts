plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Jackson.core)
   implementation(Libs.Jackson.databind)
}

apply("../publish.gradle.kts")
