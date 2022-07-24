plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Vault.core)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:vault:_")
}

apply("../publish.gradle.kts")
