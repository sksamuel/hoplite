plugins {
   kotlin("jvm")
}

dependencies {
   api(project(":hoplite-core"))
   api(project(":hoplite-watch"))
   implementation(Libs.Orbitz.consul)

   testImplementation(Libs.EmbeddedConsul.consul)
   testImplementation(project(":hoplite-consul"))
   testImplementation(project(":hoplite-json"))
   testImplementation(project(":hoplite-yaml"))
}

apply("../publish.gradle.kts")
