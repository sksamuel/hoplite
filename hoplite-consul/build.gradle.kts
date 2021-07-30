plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api(Libs.Orbitz.consul)
}

apply("../publish.gradle.kts")
