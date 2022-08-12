dependencies {
   api(Libs.Kotlin.reflect)
   implementation(project(":hoplite-fp"))
}

apply("../publish.gradle.kts")
