plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api(Libs.Hadoop.common)
}

apply("../publish.gradle.kts")
