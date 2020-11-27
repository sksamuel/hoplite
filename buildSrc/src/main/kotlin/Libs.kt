object Libs {

  const val kotlinVersion = "1.3.72"
  const val org = "com.sksamuel.hoplite"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
  }

  object Arrow {
    const val Validation = "io.arrow-kt:arrow-validation:0.11.0"
    const val Data = "io.arrow-kt:arrow-core-data:0.11.0"
  }

  object Jackson {
    const val core = "com.fasterxml.jackson.core:jackson-core:2.10.3"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.10.3"
  }

  object Kotest {
    private const val version = "4.3.1"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }
}
