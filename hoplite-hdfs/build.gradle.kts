plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api("org.apache.hadoop:hadoop-common:2.6.5")
}

apply("../publish.gradle.kts")
