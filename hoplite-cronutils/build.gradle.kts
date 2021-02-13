plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.CronUtils.utils)
}

apply("../publish.gradle.kts")
