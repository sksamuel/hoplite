plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(Libs.Aws2.regions)
}

apply("../publish.gradle.kts")
