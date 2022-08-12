plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Micrometer.prometheus)
}

apply("../publish.gradle.kts")
