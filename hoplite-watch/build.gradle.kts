dependencies {
   api(projects.hopliteCore)
//   implementation(KotlinX.coroutines.core)
   testImplementation(projects.hopliteJson)
}

apply("../publish.gradle.kts")
