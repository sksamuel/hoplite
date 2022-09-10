plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   api(libs.consul.client)
}

apply("../publish.gradle.kts")
