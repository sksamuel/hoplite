plugins {
  kotlin("jvm")
}

dependencies {
  api(project(":hoplite-core"))
  api("com.zaxxer:HikariCP:3.4.5")

  testImplementation(project(":hoplite-yaml"))
  testImplementation("org.postgresql:postgresql:42.2.16")
}

apply("../publish.gradle.kts")
