plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(KotlinX.datetime)
}

apply("../publish.gradle.kts")
