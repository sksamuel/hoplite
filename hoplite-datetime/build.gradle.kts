plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Kotlin.datetime)
}

apply("../publish.gradle.kts")
