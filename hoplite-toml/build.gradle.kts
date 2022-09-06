plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Toml.toml)
}

apply("../publish.gradle.kts")
