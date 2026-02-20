plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
   alias(libs.plugins.kotlin.serialization)
}

dependencies {
   api(projects.hopliteCore)
   implementation(libs.snakeyaml)
}
