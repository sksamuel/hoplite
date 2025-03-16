import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
   repositories {
      mavenLocal()
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      }
      maven {
         url = uri("https://plugins.gradle.org/m2/")
      }
   }
}

plugins {
   id("java")
   id("java-library")
   id("maven-publish")
   id("signing")
   alias(libs.plugins.kotlin.jvm)
}

allprojects {
   apply(plugin = "org.jetbrains.kotlin.jvm")

   group = "com.sksamuel.hoplite"
   version = Ci.version

   dependencies {
      testImplementation("io.kotest:kotest-assertions-core:5.9.1")
      testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
   }

   tasks.named<Test>("test") {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
         showStandardStreams = true
         exceptionFormat = TestExceptionFormat.FULL
      }
   }

   kotlin {
      jvmToolchain(11)
      compilerOptions.jvmTarget = JvmTarget.JVM_1_8
   }

   tasks.compileJava {
      options.release = 8
   }

   tasks.compileTestKotlin {
      compilerOptions.jvmTarget = JvmTarget.JVM_11
   }

   tasks.compileTestJava {
      options.release = 11
   }

   repositories {
      mavenLocal()
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots")
      }
   }
}
