import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
  repositories {
    jcenter()
    mavenCentral()
    maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }

//    dependencies {
//        classpath files("/home/sam/.m2/repository/io/kotest/kotest-gradle-plugin/0.2-LOCAL/kotest-gradle-plugin-0.2-LOCAL.jar")
//    }
}

plugins {
  java
  `java-library`
  id("java-library")
  id("maven-publish")
  signing
  maven
  `maven-publish`
  kotlin("jvm").version(Libs.kotlinVersion)
}

allprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
//    apply plugin: 'io.kotest'

  group = Libs.org
  version = Ci.version

  dependencies {
    testImplementation(Libs.Kotest.assertions)
    testImplementation(Libs.Kotest.junit5)
    implementation(Libs.Kotlin.stdlib)
  }

  tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
      showExceptions = true
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }

  repositories {
    mavenLocal()
    mavenCentral()
    maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
  }
}
