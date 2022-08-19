plugins {
   kotlin("plugin.serialization") version "1.6.21"
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws.core)
   api(Libs.Aws.ssm)
   api(Libs.Aws.secrets)
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
   testApi(Libs.Kotest.testContainers)
   testApi(Libs.TestContainers.localstack)
   testApi(Libs.Logback.classic)
   testApi(Libs.Slf4j.api)
}

apply("../publish.gradle.kts")
