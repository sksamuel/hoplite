plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(projects.hopliteCore)
//   implementation(KotlinX.coroutines.core)
   testImplementation(projects.hopliteJson)
}
