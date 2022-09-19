dependencies {
   api(projects.hopliteCore)
   implementation(libs.micrometer.registry.datadog)
}

apply("../publish.gradle.kts")
