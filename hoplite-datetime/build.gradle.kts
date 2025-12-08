plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}


dependencies {
   api(projects.hopliteCore)
   implementation(libs.kotlinx.datetime)
}
