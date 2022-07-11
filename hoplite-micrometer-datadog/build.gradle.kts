plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Micrometer.datadog)
}

apply("../publish.gradle.kts")
