dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   api(Libs.Aws.core)
   api(Libs.Aws.ssm)
   api(Libs.Aws.secrets)
   testApi(Libs.Kotest.testContainers)
   testApi(Libs.TestContainers.localstack)

   testApi(Libs.Logback.classic)
   testApi(Libs.Slf4j.api)
}

apply("../publish.gradle.kts")
