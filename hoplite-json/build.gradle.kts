plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Jackson.core)
   implementation(Libs.Jackson.databind)
}

apply("../publish.gradle.kts")
