plugins {
    kotlin("jvm")
}

dependencies {
    api(projects.hopliteCore)
    api(libs.hadoop.common)
}

apply("../publish.gradle.kts")
