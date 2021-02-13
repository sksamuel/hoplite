plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api(Libs.Aws.core)
    api(Libs.Aws.ssm)
}

apply("../publish.gradle.kts")
