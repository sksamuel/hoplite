dependencies {
   api(libs.kotlin.reflect)
   testImplementation(libs.postgresql)
   api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
   testImplementation("org.testcontainers:testcontainers:1.18.1")
   testImplementation("org.testcontainers:postgresql:1.18.1")
}

apply("../publish.gradle.kts")
