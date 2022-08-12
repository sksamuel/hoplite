plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   api(Libs.Orbitz.consul)
}

apply("../publish.gradle.kts")
