plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Vault.core)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:vault:1.17.3")
}

apply("../publish.gradle.kts")
