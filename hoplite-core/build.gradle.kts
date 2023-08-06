dependencies {
   api(libs.kotlin.reflect)
   testImplementation(libs.postgresql)
   api(libs.coroutines.core)
   api(libs.coroutines.jdk8)
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
   testImplementation("org.testcontainers:testcontainers:1.18.1")
   testImplementation(libs.testcontainers.postgresql)
}

apply("../publish.gradle.kts")
