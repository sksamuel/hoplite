plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   api(libs.kotlin.reflect)
   testImplementation(libs.postgresql)
   api(libs.coroutines.core)
   api(libs.coroutines.jdk8)
   testImplementation(libs.testcontainers.base)
   testImplementation(libs.testcontainers.postgresql)
}
