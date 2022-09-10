plugins {
   kotlin("jvm")
}

dependencies {
   api(libs.kotlin.reflect)
}

apply("../publish.gradle.kts")
