plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws.core)
   api(Libs.Aws.ssm)
   api(Libs.Aws.secrets)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:localstack:_")

   testApi("ch.qos.logback:logback-classic:_")
   testApi("org.slf4j:slf4j-api:_")
}

apply("../publish.gradle.kts")
