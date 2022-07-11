plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Micrometer.statsd)
}

apply("../publish.gradle.kts")
