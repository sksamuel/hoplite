plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws.core)
   api(Libs.Aws.ssm)
   api(Libs.Aws.secrets)
   testApi(Libs.Kotest.testContainers)
   testApi("org.testcontainers:localstack:1.17.3")

   testApi("ch.qos.logback:logback-classic:1.2.11")
   testApi("org.slf4j:slf4j-api:1.7.36")
}

apply("../publish.gradle.kts")
