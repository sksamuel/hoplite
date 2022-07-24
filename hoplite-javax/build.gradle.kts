plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   testImplementation(project(":hoplite-toml"))
   testImplementation(project(":hoplite-yaml"))
   testImplementation(project(":hoplite-json"))
}

apply("../publish.gradle.kts")
