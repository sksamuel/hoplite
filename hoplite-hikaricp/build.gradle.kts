plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   api(Libs.Hikari.core)

   testImplementation(project(":hoplite-yaml"))
   testImplementation(Libs.Postgres.driver)
}

apply("../publish.gradle.kts")
