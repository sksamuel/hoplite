plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   api(Libs.CronUtils.utils)
}

apply("../publish.gradle.kts")
