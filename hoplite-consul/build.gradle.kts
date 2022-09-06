plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   api(Libs.Orbitz.consul)
}

apply("../publish.gradle.kts")
