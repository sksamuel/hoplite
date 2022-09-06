dependencies {
   api(projects.hopliteCore)
   implementation(Libs.Kotlin.coroutines)
   testImplementation(projects.hopliteJson)
}

apply("../publish.gradle.kts")
