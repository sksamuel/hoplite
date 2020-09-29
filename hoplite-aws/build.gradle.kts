plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api("com.amazonaws:aws-java-sdk-core:1.11.708")
    api("com.amazonaws:aws-java-sdk-ssm:1.11.708")
}

apply("../publish.gradle.kts")
