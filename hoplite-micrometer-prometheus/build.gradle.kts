plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(Libs.Micrometer.prometheus)
}

apply("../publish.gradle.kts")
