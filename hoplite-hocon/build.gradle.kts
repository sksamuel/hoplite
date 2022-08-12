dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   implementation(Libs.Typesafe.config)
}

apply("../publish.gradle.kts")
