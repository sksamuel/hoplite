plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":hoplite-core"))
    api(Libs.Aws.core)
    api(Libs.Aws.ssm)
    api(Libs.Aws.secrets)
}

apply("../publish.gradle.kts")
