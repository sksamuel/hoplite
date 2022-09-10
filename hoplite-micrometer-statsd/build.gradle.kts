plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.micrometer.registry.statsd)
}

apply("../publish.gradle.kts")
