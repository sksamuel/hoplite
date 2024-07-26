dependencies {
   api(libs.kotlin.reflect)
   testImplementation(libs.postgresql)
   api(libs.coroutines.core)
   api(libs.coroutines.jdk8)
   testImplementation(libs.kotest.extensions.testcontainers)
   testImplementation(libs.testcontainers)
   testImplementation(libs.testcontainers.postgresql)
}

apply("../publish.gradle.kts")
