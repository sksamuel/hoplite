plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Micrometer.statsd)
}

apply("../publish.gradle.kts")
