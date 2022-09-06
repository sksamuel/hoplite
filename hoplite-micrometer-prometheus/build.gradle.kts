plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Micrometer.prometheus)
}

apply("../publish.gradle.kts")
