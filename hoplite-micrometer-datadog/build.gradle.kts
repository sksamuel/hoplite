plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Micrometer.datadog)
}

apply("../publish.gradle.kts")
