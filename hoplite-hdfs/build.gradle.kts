plugins {
    kotlin("jvm")
}

dependencies {
    api(projects.hopliteCore)
    api(Libs.Hadoop.common)
}

apply("../publish.gradle.kts")
