dependencies {
   implementation(project(":hoplite-fp"))
   api(project(":hoplite-core"))
   api(Libs.Aws2.regions)
}

apply("../publish.gradle.kts")
