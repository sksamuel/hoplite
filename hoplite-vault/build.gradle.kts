plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Vault.core)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:vault:_")
}

apply("../publish.gradle.kts")
