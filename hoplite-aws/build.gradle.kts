plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws.core)
   api(Libs.Aws.ssm)
   api(Libs.Aws.secrets)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:localstack:1.16.3")
}

apply("../publish.gradle.kts")
