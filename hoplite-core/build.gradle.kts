plugins {
  kotlin("multiplatform")
}

repositories {
  mavenCentral()
}

kotlin {

  targets {
    jvm {
      compilations.all {
        kotlinOptions {
          jvmTarget = "1.8"
        }
      }
    }
    js(BOTH) {
      browser()
      nodejs()
    }

    linuxX64()

    mingwX64()

    macosX64()
    tvos()
    watchos()

    iosX64()
    iosArm64()
    iosArm32()
  }

  targets.all {
    compilations.all {
      kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
      }
    }
  }

  sourceSets {

    val commonMain by getting {
      dependencies {
        implementation(kotlin("reflect"))
      }
    }

    val jvmMain by getting {
      dependsOn(commonMain)
      dependencies {
      }
    }

    val jvmTest by getting {
      dependsOn(jvmMain)
      dependencies {
        implementation(Libs.Kotest.assertions)
        implementation(Libs.Kotest.junit5)
        implementation(Libs.Kotlin.stdlib)
      }
    }

    val desktopMain by creating {
      dependsOn(commonMain)
    }

    val macosX64Main by getting {
      dependsOn(desktopMain)
    }

    val mingwX64Main by getting {
      dependsOn(desktopMain)
    }

    val linuxX64Main by getting {
      dependsOn(desktopMain)
    }

    val iosX64Main by getting {
      dependsOn(desktopMain)
    }

    val iosArm64Main by getting {
      dependsOn(desktopMain)
    }

    val iosArm32Main by getting {
      dependsOn(desktopMain)
    }

    val watchosMain by getting {
      dependsOn(desktopMain)
    }

    val tvosMain by getting {
      dependsOn(desktopMain)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.apiVersion = "1.4"
}

apply("../publish.gradle.kts")
