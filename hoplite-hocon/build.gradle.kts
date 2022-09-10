dependencies {
   api(projects.hopliteCore)
   implementation(libs.config)
}

apply("../publish.gradle.kts")
