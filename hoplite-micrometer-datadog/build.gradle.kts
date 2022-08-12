plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Micrometer.datadog)
}

apply("../publish.gradle.kts")
