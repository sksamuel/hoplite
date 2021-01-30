object Libs {

  const val kotlinVersion = "1.4.21"
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
    const val core = "com.fasterxml.jackson.core:jackson-core:2.11.3"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.11.3"
  }

  object Kotest {
    private const val version = "4.4.0.RC3"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }

  object Vavr {
    const val kotlin = "io.vavr:vavr-kotlin:0.10.2"
  }
}
