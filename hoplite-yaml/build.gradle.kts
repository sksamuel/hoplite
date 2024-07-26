plugins {
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.snakeyaml)
}

apply("../publish.gradle.kts")
