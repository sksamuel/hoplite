plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   implementation(project(":hoplite-fp"))
   testImplementation(project(":hoplite-toml"))
   testImplementation(project(":hoplite-yaml"))
   testImplementation(project(":hoplite-json"))
}

apply("../publish.gradle.kts")
