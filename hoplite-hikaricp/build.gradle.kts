plugins {
   kotlin("jvm")
}

dependencies {
   api(projects.hopliteCore)
   api(Libs.Hikari.core)

   testImplementation(projects.hopliteYaml)
   testImplementation(Libs.Postgres.driver)
}

apply("../publish.gradle.kts")
