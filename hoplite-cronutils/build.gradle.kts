plugins {
    kotlin("jvm")
}

dependencies {
    api (project(":hoplite-core"))
    api ("com.cronutils:cron-utils:9.0.2")
}

apply("../publish.gradle.kts")
